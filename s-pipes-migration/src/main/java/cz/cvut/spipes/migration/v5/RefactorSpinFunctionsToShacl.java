package cz.cvut.spipes.migration.v5;

import cz.cvut.spipes.spin.vocabulary.SP;
import cz.cvut.spipes.spin.vocabulary.SPIN;
import cz.cvut.spipes.spin.vocabulary.SPL;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.util.List;

/**
 * Step 2: Refactor SPIN Functions to SHACL.
 *
 * - spin:Function → sh:SPARQLFunction
 * - spin:body [ a sp:Select ; sp:text "..." ] → sh:select "..."
 * - spin:returnType → sh:returnType
 * - spin:constraint → sh:parameter
 * - Within parameter blank nodes:
 *   - Remove rdf:type spl:Argument
 *   - spl:predicate → sh:path
 *   - spl:valueType → sh:datatype
 *   - rdfs:comment → sh:description (preserve language tag)
 *   - rdfs:label → sh:name (preserve language tag)
 *   - spl:optional → sh:optional
 * - Add sh: prefix and owl:imports shacl to ontology
 */
public class RefactorSpinFunctionsToShacl {

    private static final String SH_NS = "http://www.w3.org/ns/shacl#";
    private static final Resource SH_SPARQLFunction = ResourceFactory.createResource(SH_NS + "SPARQLFunction");
    private static final Property SH_select = ResourceFactory.createProperty(SH_NS + "select");
    private static final Property SH_returnType = ResourceFactory.createProperty(SH_NS + "returnType");
    private static final Property SH_parameter = ResourceFactory.createProperty(SH_NS + "parameter");
    private static final Property SH_path = ResourceFactory.createProperty(SH_NS + "path");
    private static final Property SH_datatype = ResourceFactory.createProperty(SH_NS + "datatype");
    private static final Property SH_description = ResourceFactory.createProperty(SH_NS + "description");
    private static final Property SH_name = ResourceFactory.createProperty(SH_NS + "name");
    private static final Property SH_optional = ResourceFactory.createProperty(SH_NS + "optional");

    private static final Resource SHACL_ONTOLOGY = ResourceFactory.createResource("http://www.w3.org/ns/shacl");

    public void apply(Model model) {
        refactorSpinFunctions(model);
        refactorAllSpinConstraints(model);
        addShaclPrefixAndImport(model);
    }

    private void refactorSpinFunctions(Model model) {
        List<Resource> functions = model.listSubjectsWithProperty(RDF.type, SPIN.Function).toList();
        for (Resource function : functions) {
            refactorFunctionType(model, function);
            refactorBody(model, function);
            refactorReturnType(model, function);
        }
    }

    private void refactorAllSpinConstraints(Model model) {
        List<Resource> subjects = model.listSubjectsWithProperty(SPIN.constraint).toList();
        for (Resource subject : subjects) {
            refactorConstraints(model, subject);
        }
    }

    private void refactorFunctionType(Model model, Resource function) {
        model.remove(function, RDF.type, SPIN.Function);
        model.add(function, RDF.type, SH_SPARQLFunction);
    }

    private void refactorBody(Model model, Resource function) {
        List<Statement> bodyStmts = model.listStatements(function, SPIN.body, (RDFNode) null).toList();
        for (Statement bodyStmt : bodyStmts) {
            RDFNode bodyNode = bodyStmt.getObject();
            if (bodyNode.isResource()) {
                Resource bodyResource = bodyNode.asResource();
                Statement textStmt = bodyResource.getProperty(SP.text);
                if (textStmt != null) {
                    RDFNode textValue = textStmt.getObject();
                    model.add(function, SH_select, textValue);
                }
                if (bodyResource.isAnon()) {
                    RemoveSpinRdfQueries.removeBlankNodeTree(model, bodyResource);
                }
            }
            model.remove(bodyStmt);
        }
    }

    private void refactorReturnType(Model model, Resource function) {
        List<Statement> stmts = model.listStatements(function, SPIN.returnType, (RDFNode) null).toList();
        for (Statement stmt : stmts) {
            model.add(function, SH_returnType, stmt.getObject());
            model.remove(stmt);
        }
    }

    private void refactorConstraints(Model model, Resource function) {
        List<Statement> constraintStmts = model.listStatements(function, SPIN.constraint, (RDFNode) null).toList();
        for (Statement constraintStmt : constraintStmts) {
            RDFNode paramNode = constraintStmt.getObject();
            model.add(function, SH_parameter, paramNode);
            model.remove(constraintStmt);

            if (paramNode.isResource()) {
                refactorParameterBlankNode(model, paramNode.asResource());
            }
        }
    }

    private void refactorParameterBlankNode(Model model, Resource param) {
        model.removeAll(param, RDF.type, SPL.Argument);

        replaceProperty(model, param, SPL.predicate, SH_path);
        replaceProperty(model, param, SPL.valueType, SH_datatype);
        replaceProperty(model, param, SPL.optional, SH_optional);
        replaceProperty(model, param, RDFS.comment, SH_description);
        replaceProperty(model, param, RDFS.label, SH_name);
    }

    private void replaceProperty(Model model, Resource subject, Property oldProp, Property newProp) {
        List<Statement> stmts = model.listStatements(subject, oldProp, (RDFNode) null).toList();
        for (Statement stmt : stmts) {
            model.add(subject, newProp, stmt.getObject());
            model.remove(stmt);
        }
    }

    private void addShaclPrefixAndImport(Model model) {
        if (!modelUsesShaclNamespace(model)) {
            return;
        }

        model.setNsPrefix("sh", SH_NS);

        List<Resource> ontologies = model.listSubjectsWithProperty(RDF.type, OWL.Ontology).toList();
        for (Resource ontology : ontologies) {
            if (!model.contains(ontology, OWL.imports, SHACL_ONTOLOGY)) {
                model.add(ontology, OWL.imports, SHACL_ONTOLOGY);
            }
        }
    }

    private boolean modelUsesShaclNamespace(Model model) {
        return model.listStatements().toList().stream().anyMatch(stmt -> {
            if (stmt.getPredicate().getURI().startsWith(SH_NS)) return true;
            if (stmt.getSubject().isURIResource() && stmt.getSubject().getURI().startsWith(SH_NS)) return true;
            if (stmt.getObject().isURIResource() && stmt.getObject().asResource().getURI().startsWith(SH_NS)) return true;
            return false;
        });
    }
}
