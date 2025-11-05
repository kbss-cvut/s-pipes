package cz.cvut.spipes.riot;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.system.PrefixMapFactory;
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
        try (var in = getClass().getResourceAsStream("/riot/format-test-input.ttl")) {
            assertNotNull(in);
            model.read(in, null, "TURTLE");
        }

        // We can't compare the output with the same input file, because of how blank nodes are serialised.
        var expected = readUtf8("/riot/format-test-output.ttl");
        var actual   = writeToString(model);

        assertEquals(expected, actual);
    }

    @Test
    void writeScriptRespectsDependencies() throws IOException {
        var model = ModelFactory.createDefaultModel();
        try (var in = getClass().getResourceAsStream("/riot/sm-next-test.ttl")) {
            assertNotNull(in);
            model.read(in, null, "TURTLE");
        }

        var expected = readUtf8("/riot/sm-next-test.ttl");
        var actual   = writeToString(model);

        assertEquals(expected, actual);
    }

    private String writeToString(Model model) throws IOException {
        try (var baos = new ByteArrayOutputStream()) {
            SPipesTurtleWriter writer = new SPipesTurtleWriter();
            writer.write(baos, model.getGraph(), PrefixMapFactory.create(model.getGraph().getPrefixMapping()), null, null);
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