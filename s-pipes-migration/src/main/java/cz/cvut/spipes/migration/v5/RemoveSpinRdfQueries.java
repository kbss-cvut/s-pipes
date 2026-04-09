package cz.cvut.spipes.migration.v5;

import cz.cvut.spipes.spin.vocabulary.SP;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.util.List;
import java.util.Set;

/**
 * Step 1: Remove SPIN RDF SPARQL queries.
 *
 * - Removes sp:where, sp:templates, sp:resultVariables, sp:values, sp:deletePattern, and sp:insertPattern
 *   triples and their reachable blank node trees from resources typed as sp:Ask, sp:Select, sp:Construct, or sp:Modify
 * - Removes rdfs:comment from those resources
 * - Fails if any of those resources does not have sp:text
 */
public class RemoveSpinRdfQueries {

    private static final Set<Resource> QUERY_TYPES = Set.of(SP.Ask, SP.Select, SP.Construct, SP.Modify);

    private static final List<Property> PROPERTIES_TO_REMOVE = List.of(
        SP.where, SP.templates, SP.resultVariables, SP.values, SP.deletePattern, SP.insertPattern
    );

    public void apply(Model model) {
        ensureSpTextExists(model);
        for (Property property : PROPERTIES_TO_REMOVE) {
            removePropertyTreesFromQueryTypes(model, property);
        }
        removeCommentsFromQueryTypes(model);
    }

    private void ensureSpTextExists(Model model) {
        for (Resource queryType : QUERY_TYPES) {
            List<Resource> subjects = model.listSubjectsWithProperty(RDF.type, queryType).toList();
            for (Resource subject : subjects) {
                if (!subject.hasProperty(SP.text) && !isSubQuery(model, subject)) {
                    throw new RuntimeException(
                        "Cannot migrate " + toPrefixedName(model, queryType) + " resource "
                            + describeResource(model, subject)
                            + " because it does not have sp:text property."
                    );
                }
            }
        }
    }

    private String describeResource(Model model, Resource resource) {
        if (resource.isURIResource()) {
            return toPrefixedName(model, resource);
        }
        List<Statement> refs = model.listStatements(null, null, resource).toList();
        if (!refs.isEmpty()) {
            Statement ref = refs.get(0);
            return "(referenced by " + toPrefixedName(model, ref.getSubject()) + " via " + toPrefixedName(model, ref.getPredicate()) + ")";
        }
        return "(anonymous blank node)";
    }

    private String toPrefixedName(Model model, Resource resource) {
        String uri = resource.getURI();
        String qname = model.qnameFor(uri);
        if (qname != null) {
            return qname;
        }
        return "<" + uri + ">";
    }

    private boolean isSubQuery(Model model, Resource resource) {
        List<Statement> refs = model.listStatements(null, null, resource).toList();
        for (Statement ref : refs) {
            if (SP.query.equals(ref.getPredicate())
                && ref.getSubject().hasProperty(RDF.type, SP.SubQuery)) {
                return true;
            }
        }
        return false;
    }

    private void removePropertyTreesFromQueryTypes(Model model, Property property) {
        for (Resource queryType : QUERY_TYPES) {
            List<Resource> subjects = model.listSubjectsWithProperty(RDF.type, queryType).toList();
            for (Resource subject : subjects) {
                List<Statement> stmts = model.listStatements(subject, property, (RDFNode) null).toList();
                for (Statement stmt : stmts) {
                    RDFNode object = stmt.getObject();
                    if (object.isAnon()) {
                        removeBlankNodeTree(model, object.asResource());
                    }
                    model.remove(stmt);
                }
            }
        }
    }

    private void removeCommentsFromQueryTypes(Model model) {
        for (Resource queryType : QUERY_TYPES) {
            List<Resource> subjects = model.listSubjectsWithProperty(RDF.type, queryType).toList();
            for (Resource subject : subjects) {
                model.removeAll(subject, RDFS.comment, null);
            }
        }
    }

    static void removeBlankNodeTree(Model model, Resource blankNode) {
        if (!blankNode.isAnon()) return;

        List<Statement> stmts = model.listStatements(blankNode, null, (RDFNode) null).toList();
        for (Statement stmt : stmts) {
            RDFNode obj = stmt.getObject();
            if (obj.isAnon()) {
                removeBlankNodeTree(model, obj.asResource());
            }
            model.remove(stmt);
        }
    }
}
