package cz.cvut.spipes.rest.handler;

import com.github.jsonldjava.core.JsonLdError;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;


public class ErrorValidationResponseTest {

    @Test
    public void testGetFramedAndCompactedJsonLdReturnsFramedOrderedEvidences() throws JsonLdError, IOException {
        String module = "http://onto.fel.cvut.cz/ontologies/ava/tabular-data-0.1/rdfize-input-data";
        String message = "?row within ?column has empty value";
        String failedQuery = """
                # ?row within ?column has empty value
                SELECT ?row ?column
                WHERE {

                  ?r csvw:describes ?rd .
                  ?r csvw:rownum ?row .

                  ?c a csvw:Column .
                  ?c kbss-csvw:property ?columnProperty .
                  ?c csvw:title ?column .

                  FILTER NOT EXISTS {
                    ?rd ?columnProperty ?valueNE .
                  }

                  FILTER(?columnProperty in ( :Failure_condition_label__CS_, :Failure_condition_group_id, :SNS_code ))
                }
                """;


        // Example evidence data
        Map<String, RDFNode> evidence1 = new HashMap<>();
        evidence1.put("row", ResourceFactory.createPlainLiteral(String.valueOf(6)));
        evidence1.put("column", ResourceFactory.createPlainLiteral("SNS code"));

        Map<String, RDFNode> evidence2 = new HashMap<>();
        evidence2.put("row", ResourceFactory.createPlainLiteral(String.valueOf(3)));
        evidence2.put("column", ResourceFactory.createPlainLiteral("Failure condition group id"));
        List<Map<String, RDFNode>> evidences = List.of(evidence1, evidence2);

        ErrorValidationResponse response = new ErrorValidationResponse(
                module,
                message,
                failedQuery,
                evidences
        );

        @SuppressWarnings("unchecked")
        LinkedHashMap<String, Object> jsonLd = (LinkedHashMap<String, Object>) response.getFramedAndCompactedJsonLd();

        assertNotNull(jsonLd);

        assertEquals(module, jsonLd.get("module").toString());
        assertEquals(failedQuery, jsonLd.get("constraintQuery").toString());
        assertEquals(message, jsonLd.get("message").toString());


        @SuppressWarnings("unchecked")
        List<Map<String, RDFNode>> jsonLdEvidences = (List<Map<String, RDFNode>>) jsonLd.get("constraintFailureEvidences");
        List<Map<String, RDFNode>> filteredEvidences = jsonLdEvidences.stream()
                .map(e -> e.entrySet().stream()
                        .filter(entry -> !Objects.equals(entry.getKey(), "@id"))
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> (RDFNode) ResourceFactory.createPlainLiteral(String.valueOf(entry.getValue()))))
                )
                .toList();
        assertEquals(evidences, filteredEvidences);
    }
}
