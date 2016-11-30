package cz.cvut.sempipes.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileUtils;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Miroslav Blasko on 30.7.16.
 */
public class JenaUtils {

    public static Model readModelFromString(String modelText, String lang) {
        Model model = ModelFactory.createDefaultModel();

        return model.read(new ByteArrayInputStream(modelText.getBytes()), null, lang);
    }


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


    /**
     * Compute hash of an dataset considering semantics of RDF,
     * i.e. hashes of two RDF models are same iff RDF models are isomorphic.
     *
     * TODO this does not implement correct algorithm (different graphs might return same hash),
     * although should suffice in many real cases. See  http://aidanhogan.com/skolem/ to find more reliable algorithm.
     * @param model RDF graph to compute hash
     * @return computed hash
     */
    public static String computeHash(Model model) {
        StringBuilder modelMetadataBuff = new StringBuilder();

        Comparator<? super Resource> uriComparator =
                (r1, r2) -> {
                    return r1.getURI().compareTo(r2.getURI());
                };

        long statementsSize = model.size();
        List<Resource> subjectResources = new ArrayList<>(
                model.listSubjects()
                        .filterKeep(RDFNode::isURIResource)
                        .toList()
        );
        subjectResources.sort(uriComparator);
        List<Resource> objectResources = new ArrayList<>(
                model.listObjects()
                        .filterKeep(RDFNode::isURIResource)
                        .mapWith(RDFNode::asResource)
                        .toList()
        );
        objectResources.sort(uriComparator);

        modelMetadataBuff
                .append("No. of statements: ").append(statementsSize).append("\n")
                .append("Subjects: ").append(subjectResources.toString()).append("\n")
                .append("Objects: ").append(objectResources.toString()).append("\n");


        return DigestUtils.md5Hex(modelMetadataBuff.toString());
    }
}
