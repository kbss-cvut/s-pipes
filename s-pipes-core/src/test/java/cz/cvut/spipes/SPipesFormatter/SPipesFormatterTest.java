package cz.cvut.spipes.SPipesFormatter;

import cz.cvut.spipes.util.JenaUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SPipesFormatterTest {

    @Test
    void writeScriptPreservesFormatting() throws IOException {
        var model = ModelFactory.createDefaultModel();
        try (var in = getClass().getResourceAsStream("/SPipesFormatter/format-test-input.ttl")) {
            assertNotNull(in);
            model.read(in, null, "TURTLE");
        }

        // We can't compare the output with the same input file, because of how blank nodes are serialised.
        var expected = readUtf8("/SPipesFormatter/format-test-output.ttl");
        var actual   = writeToString(model);

        assertEquals(expected, actual);
    }

    @Test
    void writeScriptRespectsDependencies() throws IOException {
        var model = ModelFactory.createDefaultModel();
        try (var in = getClass().getResourceAsStream("/SPipesFormatter/sm-next-test-input.ttl")) {
            assertNotNull(in);
            model.read(in, null, "TURTLE");
        }

        // We can't compare the output with the same input file, because of how blank nodes are serialised.
        var expected = readUtf8("/SPipesFormatter/sm-next-test-output.ttl");
        var actual   = writeToString(model);

        assertEquals(expected, actual);
    }

    private String writeToString(Model model) throws IOException {
        try (var baos = new ByteArrayOutputStream()) {
            JenaUtils.writeScript(baos, model);
            return baos.toString(StandardCharsets.UTF_8);
        }
    }

    private String readUtf8(String path) throws IOException {
        try (var in = getClass().getResourceAsStream(path)) {
            assertNotNull(in, path + " not found");
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

}