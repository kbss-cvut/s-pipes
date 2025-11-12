package cz.cvut.spipes.util;

import cz.cvut.spipes.riot.CustomLangs;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.compose.MultiUnion;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.*;
import org.apache.jena.util.FileUtils;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
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
     * Compute hash of a dataset considering the semantics of RDF,
     * i.e., hashes of two RDF models are same iff RDF models are isomorphic.
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
                Comparator.comparing(Resource::getURI);

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
                outputModel::add
        );
        return outputModel;
    }

    public static void saveModelToTemporaryFile(@NotNull Model model) {
        try {
            Path file = Files.createTempFile("model-output-", ".ttl");
            log.debug("Saving model to temporary file {} ...", file.toString());
            JenaUtils.write(Files.newOutputStream(file.toFile().toPath()), model);
        } catch (IOException e) {
            log.error(e.getMessage());
        }

    }

    /**
     * Common method to write rdf data within SPipes to specified output stream.
     * To write SPipes scripts use {@link #writeScript(OutputStream, Model)} instead.
     *
     * @param outputStream output stream to write data to
     * @param model rdf data to write
     */
    public static void write(OutputStream outputStream, Model model){
        write(outputStream, model, Lang.TTL);
    }

    /**
     * Common method to rdf data within SPipes to specified output stream.
     * To write SPipes scripts use {@link #writeScript(OutputStream, Model)} instead.
     *
     * @param outputStream output stream to write data to
     * @param model rdf data to write
     * @param lang format of the output data
     */
    public static void write(OutputStream outputStream, Model model, Lang lang){
        RDFWriter.create()
            .source(model)
            .lang(lang)
            .output(outputStream);
    }

    /**
     * Common method to write SPipes scripts to specified output stream.
     * To write generic rdf data use {@link #write(OutputStream, Model)} instead.
     *
     * @param outputStream output stream to write data to
     * @param model rdf data to write
     * @throws IllegalArgumentException if model is not valid SPipes script
     */
    public static void writeScript(OutputStream outputStream, Model model) {
        RDFWriter.create()
                .format(new RDFFormat(CustomLangs.SPIPES_TURTLE))
                .source(model)
                .output(outputStream);
    }

    /**
     * Hotfix for default namespace bug in Jena: default namespace of an import is returned when <code>model</code>
     * does not declare it.
     *
     * @param model the Model to operate on
     * @param prefix the prefix to get the URI of
     * @return the URI of prefix
     */
    public static String getNsPrefixURI(Model model, String prefix) {
        if ("".equals(prefix) && model.getGraph() instanceof MultiUnion) {
            Graph baseGraph = ((MultiUnion)model.getGraph()).getBaseGraph();
            if(baseGraph != null) {
                return baseGraph.getPrefixMapping().getNsPrefixURI(prefix);
            }
            else {
                return model.getNsPrefixURI(prefix);
            }
        }
        else {
            return model.getNsPrefixURI(prefix);
        }
    }

    public static Model getModel(String resource, String ontologyIRI) {
        Model model = ModelFactory.createDefaultModel();
        InputStream is = resource == null
                ? null
                : JenaUtils.class.getResourceAsStream(resource);
        return is == null
            ? model.read(ontologyIRI)
            : model.read(is, null, FileUtils.langTurtle);
    }

    // TODO - Decide if reified statements should be supported or replaced with something else, e.g. RDF-star. Based on
    //  the decision retain or rewrite HOTFIX methods and their usage. Delete "HOTFIX" from comments.
    /**
     * HOTFIX - for model.listReifiedStatements()
     *
     * @param m
     * @return iterator of resources which have the RDF.subject, RDF.predicate and RDF.object properties
     */
    public static ExtendedIterator<Resource> listStatementSubjectOfReifiedStatements(Model m){
        return m.listResourcesWithProperty(RDF.object).filterKeep(r -> r.hasProperty(RDF.subject) && r.hasProperty(RDF.predicate));
    }

    /**
     * HOTFIX - adding a reified statement represented by <code>rs</code> resource to model as statement
     *
     * @param m
     * @return iterator of resources which have the RDF.object property
     */
    public static void addStatementRepresentedByResource(Model m, org.apache.jena.rdf.model.Resource rs){
        m.add(rs.getPropertyResourceValue(RDF.subject), rs.getPropertyResourceValue(RDF.predicate).as(Property.class), rs.getProperty(RDF.object).getObject());
    }

    /**
     * HOTFIX - add the reified statement of <code>st</code> to the model to the <code>st</code>
     * @param st
     * @return the resource representing the statement
     */
    public static Resource addReifiedStatement(Statement st) {
        return addReifiedStatement(st.getModel(),st);
    }

    /**
     * HOTFIX - add the reified statement of <code>st</code> to the <code>m</code>.
     * @param st
     * @return the resource representing the statement
     */
    public static Resource addReifiedStatement(Model m, Statement st) {

        m.add(st);

        Resource stR = m.createResource();
        stR
                .addProperty(RDF.type, RDF.Statement)
                .addProperty(RDF.subject, st.getSubject())
                .addProperty(RDF.predicate, st.getPredicate())
                .addProperty(RDF.object, st.getObject());
        return stR;
    }
}
