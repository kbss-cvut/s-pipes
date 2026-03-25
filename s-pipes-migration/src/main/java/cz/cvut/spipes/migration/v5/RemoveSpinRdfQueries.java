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
 * - Removes sp:where and sp:templates triples and their reachable blank node trees
 * - Removes rdfs:comment from resources typed as sp:Ask, sp:Select, or sp:Construct
 */
public class RemoveSpinRdfQueries {

    private static final Set<Resource> QUERY_TYPES = Set.of(SP.Ask, SP.Select, SP.Construct);

    public void apply(Model model) {
        removePropertyTreesFromQueryTypes(model, SP.where);
        removePropertyTreesFromQueryTypes(model, SP.templates);
        removeCommentsFromQueryTypes(model);
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
