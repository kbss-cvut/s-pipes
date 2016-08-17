package cz.cvut.sempipes.util;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

/**
 * Created by Miroslav Blasko on 30.7.16.
 */
public class JenaUtils {

    // TODO what if OWL ontology is missing
    public static String getBaseUri(Model model) {
        ResIterator it = model.listResourcesWithProperty(RDF.type, OWL.Ontology);
        if (!it.hasNext()) {
            //TODO wrong ?
            return null;
        }
        String baseURI = it.nextResource().toString();
        return baseURI;
    }
}
