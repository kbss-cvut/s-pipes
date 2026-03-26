package cz.cvut.spipes.migration.cli;

import cz.cvut.spipes.util.JenaUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MigrateCLITest {

    @TempDir
    Path tempDir;

    private Path preformattedFile;
    private Path nonPreformattedFile;

    /**
     * Turtle content with sp:text and sp:where — migration should remove sp:where tree and rdfs:comment.
     */
    private static final String SCRIPT_WITH_SPIN = """
            @prefix owl: <http://www.w3.org/2002/07/owl#> .
            @prefix sp: <http://spinrdf.org/sp#> .
            @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .

            <http://example.org/ontology>
                a owl:Ontology ;
                owl:imports <http://onto.fel.cvut.cz/ontologies/s-pipes-lib> .

            <http://example.org/query1>
                a sp:Ask ;
                rdfs:comment "this comment should be removed" ;
                sp:text "ASK { ?s ?p ?o }" ;
                sp:where [
                    a sp:TriplePath ;
                    sp:subject [
                        sp:varName "s" ;
                    ] ;
                    sp:path [
                        sp:varName "p" ;
                    ] ;
                    sp:object [
                        sp:varName "o" ;
                    ] ;
                ] .
            """;

    @BeforeEach
    void setUp() throws IOException {
        // Create a preformatted file by writing through JenaUtils.writeScript
        Model model = ModelFactory.createDefaultModel();
        model.read(new ByteArrayInputStream(SCRIPT_WITH_SPIN.getBytes(StandardCharsets.UTF_8)), null, FileUtils.langTurtle);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JenaUtils.writeScript(baos, model);
        preformattedFile = tempDir.resolve("preformatted.ttl");
        Files.write(preformattedFile, baos.toByteArray());

        // Create a non-preformatted file — same model, but with Jena's default serialization
        nonPreformattedFile = tempDir.resolve("non-preformatted.ttl");
        Files.writeString(nonPreformattedFile, SCRIPT_WITH_SPIN);
    }

    @Test
    void migrateSkipsNonPreformattedFilesByDefault() {
        byte[] originalContent = readBytes(nonPreformattedFile);

        MigrateCLI.main(new String[]{nonPreformattedFile.toString()});

        assertArrayEquals(originalContent, readBytes(nonPreformattedFile),
            "Non-preformatted file should not be modified when --ensure-formatted-input is true (default)");
    }

    @Test
    void migrateProcessesPreformattedFile() {
        byte[] originalContent = readBytes(preformattedFile);

        MigrateCLI.main(new String[]{preformattedFile.toString()});

        assertFalse(java.util.Arrays.equals(originalContent, readBytes(preformattedFile)),
            "Preformatted file with sp:where should be modified by migration");

        // Verify the migrated file no longer contains sp:where triples
        Model migratedModel = ModelFactory.createDefaultModel();
        migratedModel.read(new ByteArrayInputStream(readBytes(preformattedFile)), null, FileUtils.langTurtle);
        assertFalse(migratedModel.contains(null, migratedModel.createProperty("http://spinrdf.org/sp#", "where")),
            "sp:where should be removed after migration");
    }

    @Test
    void migrateProcessesNonPreformattedFileWhenEnsureFormattedInputIsFalse() {
        byte[] originalContent = readBytes(nonPreformattedFile);

        MigrateCLI.main(new String[]{"--ensure-formatted-input", "false", nonPreformattedFile.toString()});

        assertFalse(java.util.Arrays.equals(originalContent, readBytes(nonPreformattedFile)),
            "Non-preformatted file should be modified when --ensure-formatted-input is false");

        Model migratedModel = ModelFactory.createDefaultModel();
        migratedModel.read(new ByteArrayInputStream(readBytes(nonPreformattedFile)), null, FileUtils.langTurtle);
        assertFalse(migratedModel.contains(null, migratedModel.createProperty("http://spinrdf.org/sp#", "where")),
            "sp:where should be removed after migration");
    }

    @Test
    void migrateSkipsAlreadyMigratedFile() throws IOException {
        // Create a preformatted file that has no sp:where (already migrated)
        String alreadyMigrated = """
                @prefix owl: <http://www.w3.org/2002/07/owl#> .
                @prefix sp: <http://spinrdf.org/sp#> .

                <http://example.org/ontology>
                    a owl:Ontology ;
                    owl:imports <http://onto.fel.cvut.cz/ontologies/s-pipes-lib> .

                <http://example.org/query1>
                    a sp:Ask ;
                    sp:text "ASK { ?s ?p ?o }" .
                """;
        Model model = ModelFactory.createDefaultModel();
        model.read(new ByteArrayInputStream(alreadyMigrated.getBytes(StandardCharsets.UTF_8)), null, FileUtils.langTurtle);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JenaUtils.writeScript(baos, model);
        Path alreadyMigratedFile = tempDir.resolve("already-migrated.ttl");
        Files.write(alreadyMigratedFile, baos.toByteArray());

        byte[] originalContent = readBytes(alreadyMigratedFile);

        MigrateCLI.main(new String[]{alreadyMigratedFile.toString()});

        assertArrayEquals(originalContent, readBytes(alreadyMigratedFile),
            "Already migrated file should not be modified");
    }

    private byte[] readBytes(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
