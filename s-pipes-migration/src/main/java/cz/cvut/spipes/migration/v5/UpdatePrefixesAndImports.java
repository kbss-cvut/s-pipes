package cz.cvut.spipes.migration.v5;

import cz.cvut.spipes.spin.vocabulary.SPIN;
import cz.cvut.spipes.spin.vocabulary.SPL;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.util.List;

/**
 * Step 4: Update prefixes and imports.
 *
 * - Removes owl:imports &lt;http://spinrdf.org/spin&gt; from ontology resources
 * - Removes spl: prefix if no triples in the model use the SPL namespace
 * - Removes spin: prefix if no triples in the model use the SPIN namespace
 * - Removes rdfs:subClassOf spin:Functions from all resources
 */
public class UpdatePrefixesAndImports {

    private static final Resource SPIN_ONTOLOGY = ResourceFactory.createResource(SPIN.BASE_URI);

    public void apply(Model model) {
        removeSpinImport(model);
        removeSubClassOfSpinFunctions(model);
        removePrefixForUnusedNamespaces(model, SPIN.NS);
        removePrefixForUnusedNamespaces(model, SPL.NS);
    }

    private void removeSpinImport(Model model) {
        List<Resource> ontologies = model.listSubjectsWithProperty(RDF.type, OWL.Ontology).toList();
        for (Resource ontology : ontologies) {
            model.remove(ontology, OWL.imports, SPIN_ONTOLOGY);
        }
    }

    private void removeSubClassOfSpinFunctions(Model model) {
        model.removeAll(null, RDFS.subClassOf, SPIN.Functions);
    }

    private void removePrefixForUnusedNamespaces(Model model, String namespace) {
        if (!isNamespaceUsed(model, namespace)) {
            String prefix = model.getNsURIPrefix(namespace);
            model.removeNsPrefix(prefix);
        }
    }

    private boolean isNamespaceUsed(Model model, String namespace) {
        return model.listStatements().toList().stream().anyMatch(stmt -> {
            if (stmt.getPredicate().getURI().startsWith(namespace)) return true;
            if (stmt.getSubject().isURIResource() && stmt.getSubject().getURI().startsWith(namespace)) return true;
            if (stmt.getObject().isURIResource() && stmt.getObject().asResource().getURI().startsWith(namespace))
                return true;
            return false;
        });
    }
}
