package cz.cvut.spipes.migration.v5;

import cz.cvut.spipes.spin.vocabulary.SP;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Step 3: Refactor SPIN expression blank nodes to sp:Expression with sp:text.
 *
 * Converts typed SPIN expression trees like:
 *   [ a sp:concat ; sp:arg1 [ sp:varName "x" ] ; sp:arg2 " " ]
 * into:
 *   [ a sp:Expression ; sp:text "concat(?x, \" \")" ]
 *
 * Also converts standalone variable references like:
 *   [ sp:varName "x" ]
 * into:
 *   [ a sp:Expression ; sp:text "?x" ]
 */
public class RefactorSpinExpressions {

    private static final Set<Resource> NON_EXPRESSION_SP_TYPES = Set.of(
        SP.Ask, SP.Select, SP.Construct, SP.Modify, SP.Describe,
        SP.Expression,
        SP.Bind, SP.Filter, SP.SubQuery, SP.Service, SP.Optional,
        SP.Union, SP.Minus, SP.NamedGraph, SP.Values,
        SP.Triple, SP.TriplePath, SP.TriplePattern, SP.TripleTemplate
    );

    private static final Property[] ARG_PROPERTIES = {
        SP.arg1, SP.arg2, SP.arg3, SP.arg4, SP.arg5
    };

    public void apply(Model model) {
        List<Resource> rootExpressions = findRootExpressions(model);
        for (Resource root : rootExpressions) {
            replaceExpression(model, root);
        }
    }

    private List<Resource> findRootExpressions(Model model) {
        List<Resource> roots = new ArrayList<>();
        for (ResIterator it = model.listSubjects(); it.hasNext(); ) {
            Resource subject = it.next();
            if (!subject.isAnon()) continue;
            if (!isSpinExpression(model, subject)) continue;
            if (isSubExpression(model, subject)) continue;
            roots.add(subject);
        }
        return roots;
    }

    private boolean isSpinExpression(Model model, Resource resource) {
        return isTypedSpinExpression(model, resource) || isStandaloneVarNameDeclaration(model, resource);
    }

    private boolean isStandaloneVarNameDeclaration(Model model, Resource resource) {
        if (!resource.hasProperty(SP.varName)) return false;
        if (resource.hasProperty(RDF.type)) return false;
        // Only convert if used as a module property value (non-sp: predicate),
        // not inside SPARQL query structures (sp:object, sp:subject, sp:predicate, etc.)
        return !isInsideStructuralContext(model, resource);
    }

    private boolean isInsideStructuralContext(Model model, Resource bnode) {
        List<Statement> refs = model.listStatements(null, null, bnode).toList();
        for (Statement ref : refs) {
            String predUri = ref.getPredicate().getURI();
            // Inside SPARQL query structure (sp:object, sp:subject, sp:predicate, sp:serviceURI, etc.)
            if (predUri.startsWith(SP.NS)) return true;
            // Inside RDF list (sp:resultVariables, sp:varNames, etc.)
            if (RDF.first.getURI().equals(predUri)) return true;
        }
        return false;
    }

    private boolean isTypedSpinExpression(Model model, Resource resource) {
        List<Statement> typeStmts = model.listStatements(resource, RDF.type, (RDFNode) null).toList();
        for (Statement stmt : typeStmts) {
            RDFNode typeNode = stmt.getObject();
            if (typeNode.isURIResource()) {
                Resource type = typeNode.asResource();
                if (type.getURI().startsWith(SP.NS) && !NON_EXPRESSION_SP_TYPES.contains(type)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSubExpression(Model model, Resource bnode) {
        List<Statement> refs = model.listStatements(null, null, bnode).toList();
        for (Statement ref : refs) {
            Property pred = ref.getPredicate();
            if (isArgProperty(pred) && ref.getSubject().isAnon()
                && isSpinExpression(model, ref.getSubject())) {
                return true;
            }
        }
        return false;
    }

    private boolean isArgProperty(Property prop) {
        for (Property argProp : ARG_PROPERTIES) {
            if (argProp.equals(prop)) return true;
        }
        return false;
    }

    private void replaceExpression(Model model, Resource rootBnode) {
        String text = toText(model, rootBnode);

        Resource newBnode = model.createResource();
        model.add(newBnode, RDF.type, SP.Expression);
        model.add(newBnode, SP.text, model.createLiteral(text));

        // Replace all references to the old blank node with the new one
        List<Statement> refs = model.listStatements(null, null, rootBnode).toList();
        for (Statement ref : refs) {
            model.add(ref.getSubject(), ref.getPredicate(), newBnode);
            model.remove(ref);
        }

        // Remove the old blank node tree
        RemoveSpinRdfQueries.removeBlankNodeTree(model, rootBnode);
    }

    private String toText(Model model, RDFNode node) {
        if (node.isLiteral()) {
            return quoteStringLiteral(node.asLiteral());
        }
        if (node.isURIResource()) {
            return toPrefixedName(model, node.asResource());
        }
        if (node.isAnon()) {
            Resource bnode = node.asResource();

            // Variable reference: [ sp:varName "x" ] → ?x
            Statement varNameStmt = bnode.getProperty(SP.varName);
            if (varNameStmt != null) {
                return "?" + varNameStmt.getString();
            }

            // Function call: [ a sp:concat ; sp:arg1 ... ] → concat(...)
            Statement typeStmt = getExpressionType(model, bnode);
            if (typeStmt != null) {
                String funcName = typeStmt.getObject().asResource().getLocalName();
                List<RDFNode> args = collectArgs(model, bnode);
                StringBuilder sb = new StringBuilder();
                sb.append(funcName).append("(");
                for (int i = 0; i < args.size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(toText(model, args.get(i)));
                }
                sb.append(")");
                return sb.toString();
            }
        }
        return node.toString();
    }

    private Statement getExpressionType(Model model, Resource bnode) {
        List<Statement> typeStmts = model.listStatements(bnode, RDF.type, (RDFNode) null).toList();
        for (Statement stmt : typeStmts) {
            RDFNode typeNode = stmt.getObject();
            if (typeNode.isURIResource()) {
                Resource type = typeNode.asResource();
                if (type.getURI().startsWith(SP.NS) && !NON_EXPRESSION_SP_TYPES.contains(type)) {
                    return stmt;
                }
            }
        }
        return null;
    }

    private List<RDFNode> collectArgs(Model model, Resource bnode) {
        List<RDFNode> args = new ArrayList<>();
        for (Property argProp : ARG_PROPERTIES) {
            Statement stmt = bnode.getProperty(argProp);
            if (stmt != null) {
                args.add(stmt.getObject());
            }
        }
        return args;
    }

    private String quoteStringLiteral(Literal literal) {
        String value = literal.getString();
        String escaped = value.replace("\\", "\\\\").replace("\"", "\\\"");
        return "\"" + escaped + "\"";
    }

    private String toPrefixedName(Model model, Resource resource) {
        String uri = resource.getURI();
        String qname = model.qnameFor(uri);
        if (qname != null) {
            return qname;
        }
        return "<" + uri + ">";
    }
}
