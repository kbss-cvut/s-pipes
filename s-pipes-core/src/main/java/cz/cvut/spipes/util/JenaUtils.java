package cz.cvut.spipes.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.jena.graph.compose.MultiUnion;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class JenaUtils {

    private static final Logger log = LoggerFactory.getLogger(JenaUtils.class);

    public static Model readModelFromString(String modelText, String lang) {
        Model model = ModelFactory.createDefaultModel();

        return model.read(new ByteArrayInputStream(modelText.getBytes()), null, lang);
    }


    // TODO what if OWL ontology is missing
    public static String getBaseUri(Model model) {

        List<Resource> resList;
        if (model.getGraph() instanceof MultiUnion) {
             resList = ModelFactory.createModelForGraph(((MultiUnion) model.getGraph()).getBaseGraph()).listResourcesWithProperty(RDF.type, OWL.Ontology).toList();
        } else {
            resList = model.listResourcesWithProperty(RDF.type, OWL.Ontology).toList();
        }


        if (resList.size() > 1) {
            throw new RuntimeException("Cannot determine base uri of a model. Possible candidates are : " + resList);
        }

        if (resList.isEmpty()) {
            return null;
        }
        return resList.get(0).toString();
    }


    /**
     * Compute hash of an dataset considering semantics of RDF,
     * i.e. hashes of two RDF models are same iff RDF models are isomorphic.
     * <p>
     * TODO this does not implement correct algorithm (different graphs might return same hash),
     * although should suffice in many real cases. See  http://aidanhogan.com/skolem/ to find more reliable algorithm.
     *
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
            .append("Subjects: ").append(subjectResources).append("\n")
            .append("Objects: ").append(objectResources).append("\n");


        return DigestUtils.md5Hex(modelMetadataBuff.toString());
    }

    // TODO  due to performance issues ModelFactory.createUnion is not used (see jena-experiments) repository
    public static Model createUnion(Model... model) {
        Model outputModel = ModelFactory.createDefaultModel();
        Stream.of(model).forEach(
            m -> outputModel.add(m)
        );
        return outputModel;
    }

    public static void saveModelToTemporaryFile(@NotNull Model model) {
        try {
            Path file = Files.createTempFile("model-output-", ".ttl");
            log.debug("Saving model to temporary file " + file.toString() + " ...");
            JenaUtils.write(Files.newOutputStream(file.toFile().toPath()), model);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void write(OutputStream outputStream, Model model){
        write(outputStream, model, Lang.TTL);
    }

    public static void write(OutputStream outputStream, Model model, Lang lang){
        RDFWriter.create()
                .source(model)
                .lang(lang)
                .output(outputStream);
    }

}
