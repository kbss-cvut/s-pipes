package cz.cvut.spipes.rest.handler;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class ErrorValidationResponse {

    private static final String S_PIPES = "http://onto.fel.cvut.cz/ontologies/s-pipes/";
    private static final String SP = "http://spinrdf.org/sp#";
    private final String module;
    private final String message;
    private final String failedQuery;
    private final List<Map<String, RDFNode>> evidences;
    private final Model model = ModelFactory.createDefaultModel();

    public ErrorValidationResponse(String module, String message, String failedQuery, List<Map<String, RDFNode>> evidences) {
        this.module = module;
        this.message = message;
        this.failedQuery = failedQuery;
        this.evidences = evidences;
    }

    public String getMessage() {
        return message;
    }

    public String getFailedQuery() {
        return failedQuery;
    }

    public List<Map<String, RDFNode>> getEvidences() {
        return evidences;
    }

    public Model getModel() {
        if (model.isEmpty()) {
            return createModel();
        }
        return model;
    }

    public Object getFramedAndCompactedJsonLd() throws JsonLdError, IOException {
        // Convert the model to JSON-LD (compact, pretty)
        StringWriter compactWriter = new StringWriter();
        RDFDataMgr.write(compactWriter, getModel(), RDFFormat.JSONLD_EXPAND_FLAT);
        String compactJsonLD = compactWriter.toString();

        // Parse the compact JSON-LD
        Object compactJsonObject = JsonUtils.fromString(compactJsonLD);

        // Define the frame
        String frameJson = generateFrame();

        // Parse the frame
        Map<String, Object> frame = (Map<String, Object>) JsonUtils.fromString(frameJson);

        // Frame the compact JSON-LD
        JsonLdOptions frameOptions = new JsonLdOptions();
        Object framedJsonObject = JsonLdProcessor.frame(compactJsonObject, frame, frameOptions);

        // Compact the framed JSON-LD with the original context
        Object compactedJsonObject = JsonLdProcessor.compact(framedJsonObject, frame, frameOptions);

        return compactedJsonObject;
    }

    private String generateFrame(){

        List<String> columnNames = evidences.stream().findAny()
            .map(Map::keySet).stream()
            .flatMap(Collection::stream)
            .toList();

        String columnPropertyTerms = columnNames.stream().map(n -> String.format("\"%s\": \"%s%s\"", n, S_PIPES, n))
            .collect(Collectors.joining(",\n    ")) + ",";

        String evidenceStructure = columnNames.stream().map(n -> String.format("\"%s\": {}", n))
            .collect(Collectors.joining(",\n    ")) + "\n";

        String frameJson = """
            {
              "@context": {
                "module": "http://onto.fel.cvut.cz/ontologies/s-pipes/module",
                "message": "http://onto.fel.cvut.cz/ontologies/s-pipes/message",
                "constraintFailureEvidences": {
                  "@id": "http://onto.fel.cvut.cz/ontologies/s-pipes/constraintFailureEvidences",
                  "@container": "@list"
                },
                "constraintQuery": "http://onto.fel.cvut.cz/ontologies/s-pipes/constraintQuery",
                %s
                "s-pipes": "http://onto.fel.cvut.cz/ontologies/s-pipes/"
              },
              "@type": "http://onto.fel.cvut.cz/ontologies/s-pipes/ValidationConstraintError",
              "constraintFailureEvidences": {
                %s
              }
            }
            """;
        return String.format(frameJson, columnPropertyTerms, evidenceStructure);
    }

    private Model createModel() {
        Resource validationError = model.createResource();

        model.setNsPrefix("s-pipes", S_PIPES);
        model.add(validationError, getP("message"), message);
        model.add(validationError, RDF.type, getR("ValidationConstraintError"));
        model.add(validationError, getP("constraintQuery"), failedQuery);

        List<RDFNode> evidenceResources = new LinkedList<>();
        evidences.forEach(e -> {
                Resource r = model.createResource();
                e.forEach((key, value) -> model.add(
                    r,
                    getP(key),
                    value
                ));
                evidenceResources.add(r);
            });
        model.add(validationError, getP("module"), module);
        Resource listOfEvidences = model.createList(evidenceResources.toArray(RDFNode[]::new));
        model.add(getP("constraintFailureEvidences"), RDFS.range, RDF.List);
        model.add(
            validationError,
            getP("constraintFailureEvidences"),
            listOfEvidences
        );

        return model;
    }

    private Resource getR(String localName) {
        return model.createResource(S_PIPES + localName);
    }

    private Property getP(String localName) {
        return model.createProperty(S_PIPES + localName);
    }
}
