package cz.cvut.spipes.migration.v5;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RemoveSpinRdfQueriesTest {

    @Test
    void applyRemovesSpinRdfTriplesAndProducesExpectedOutput() {
        Model input = loadModel("/v5/remove-spin-rdf-queries.ttl");
        Model expected = loadModel("/v5/remove-spin-rdf-queries-migrated.ttl");

        new RemoveSpinRdfQueries().apply(input);

        assertTrue(input.isIsomorphicWith(expected),
            "Migrated model should be isomorphic with expected output");
    }

    @Test
    void applyThrowsWhenSpTextIsMissing() {
        Model model = ModelFactory.createDefaultModel();
        model.read(
            new java.io.ByteArrayInputStream("""
                @prefix sp: <http://spinrdf.org/sp#> .
                @prefix : <http://example.org/> .
                :q a sp:Select ;
                    sp:where [ ] .
                """.getBytes(java.nio.charset.StandardCharsets.UTF_8)),
            null, FileUtils.langTurtle
        );

        assertThrows(RuntimeException.class, () -> new RemoveSpinRdfQueries().apply(model));
    }

    private Model loadModel(String resourcePath) {
        Model model = ModelFactory.createDefaultModel();
        try (var in = getClass().getResourceAsStream(resourcePath)) {
            assertNotNull(in, resourcePath + " not found");
            model.read(in, null, FileUtils.langTurtle);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
        return model;
    }
}
