package cz.cvut.spipes.migration.v5;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RefactorSpinExpressionsTest {

    @Test
    void applyPreservesSpUpdateAndConvertsSpinExpressions() {
        Model input = loadModel("/v5/refactor-spin-expressions.ttl");
        Model expected = loadModel("/v5/refactor-spin-expressions-migrated.ttl");

        new RefactorSpinExpressions().apply(input);

        assertTrue(input.isIsomorphicWith(expected),
            "sp:Update should be preserved and sp:concat should be converted to sp:Expression");
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
