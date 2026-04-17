package cz.cvut.spipes.migration.v5;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ReplaceSpinFunctionCallsTest {

    @Test
    void applyThrowsWhenUnmappedSpifFunctionCallFound() {
        Model model = ModelFactory.createDefaultModel();
        model.read(
            new java.io.ByteArrayInputStream("""
                @prefix sp:   <http://spinrdf.org/sp#> .
                @prefix spif: <http://spinrdf.org/spif#> .
                @prefix :     <http://example.org/> .
                :q a sp:Select ;
                    sp:text "SELECT ?label WHERE { BIND(spif:buildString('{0} - {1}', ?first, ?last) AS ?label) }" .
                """.getBytes(StandardCharsets.UTF_8)),
            null, FileUtils.langTurtle
        );

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> new ReplaceSpinFunctionCalls().apply(model));
        assertTrue(ex.getMessage().contains("spif:buildString"),
            "Error message should mention the unmapped function call");
    }

    @Test
    void applyThrowsWhenUnmappedSpFunctionCallFound() {
        Model model = ModelFactory.createDefaultModel();
        model.read(
            new java.io.ByteArrayInputStream("""
                @prefix sp: <http://spinrdf.org/sp#> .
                @prefix :   <http://example.org/> .
                :q a sp:Select ;
                    sp:text "SELECT ?x WHERE { BIND(sp:concat(?a, ?b) AS ?x) }" .
                """.getBytes(StandardCharsets.UTF_8)),
            null, FileUtils.langTurtle
        );

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> new ReplaceSpinFunctionCalls().apply(model));
        assertTrue(ex.getMessage().contains("sp:concat"),
            "Error message should mention the unmapped function call");
    }
}
