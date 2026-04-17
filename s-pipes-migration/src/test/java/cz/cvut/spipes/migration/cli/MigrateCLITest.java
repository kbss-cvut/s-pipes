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

    @Test
    void incrementalAppliesOnlyFirstEffectiveStep() throws IOException {
        // Create a file that has content for step 1 (sp:where) AND step 4 (owl:imports spin)
        // spl: prefix is declared to avoid NPE bug in UpdatePrefixesAndImports
        String scriptWithMultipleStepTargets = """
                @prefix owl: <http://www.w3.org/2002/07/owl#> .
                @prefix sp: <http://spinrdf.org/sp#> .
                @prefix spin: <http://spinrdf.org/spin#> .
                @prefix spl: <http://spinrdf.org/spl#> .
                @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .

                <http://example.org/ontology>
                    a owl:Ontology ;
                    owl:imports <http://onto.fel.cvut.cz/ontologies/s-pipes-lib> ;
                    owl:imports <http://spinrdf.org/spin> .

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
        Path file = createPreformattedFile("incremental-test.ttl", scriptWithMultipleStepTargets);
        byte[] originalContent = readBytes(file);

        MigrateCLI.main(new String[]{"--incremental", "true", file.toString()});

        // File should have changed (step 1 applied)
        assertFalse(java.util.Arrays.equals(originalContent, readBytes(file)),
            "File should be modified by incremental migration step 1");

        // Step 1 should have removed sp:where
        Model migratedModel = parseModel(readBytes(file));
        assertFalse(migratedModel.contains(null, migratedModel.createProperty("http://spinrdf.org/sp#", "where")),
            "sp:where should be removed after step 1");

        // Step 4 should NOT have been applied — owl:imports spin should still be present
        assertTrue(migratedModel.contains(
                migratedModel.createResource("http://example.org/ontology"),
                migratedModel.createProperty("http://www.w3.org/2002/07/owl#", "imports"),
                migratedModel.createResource("http://spinrdf.org/spin")),
            "owl:imports <spin> should still be present (step 4 not applied)");
    }

    @Test
    void incrementalSecondRunAppliesNextStep() throws IOException {
        // Same file with content for step 1 and step 4
        // spl: prefix is declared to avoid NPE bug in UpdatePrefixesAndImports
        String scriptWithMultipleStepTargets = """
                @prefix owl: <http://www.w3.org/2002/07/owl#> .
                @prefix sp: <http://spinrdf.org/sp#> .
                @prefix spin: <http://spinrdf.org/spin#> .
                @prefix spl: <http://spinrdf.org/spl#> .
                @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .

                <http://example.org/ontology>
                    a owl:Ontology ;
                    owl:imports <http://onto.fel.cvut.cz/ontologies/s-pipes-lib> ;
                    owl:imports <http://spinrdf.org/spin> .

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
        Path file = createPreformattedFile("incremental-two-runs.ttl", scriptWithMultipleStepTargets);

        // First run — applies step 1
        MigrateCLI.main(new String[]{"--incremental", "true", file.toString()});
        byte[] afterFirstRun = readBytes(file);

        // Second run — should advance past step 1 and apply a later step
        MigrateCLI.main(new String[]{"--incremental", "true", file.toString()});
        byte[] afterSecondRun = readBytes(file);

        assertFalse(java.util.Arrays.equals(afterFirstRun, afterSecondRun),
            "Second incremental run should apply a further migration step");

        // After second run, owl:imports spin should be removed (step 4)
        Model migratedModel = parseModel(afterSecondRun);
        assertFalse(migratedModel.contains(
                migratedModel.createResource("http://example.org/ontology"),
                migratedModel.createProperty("http://www.w3.org/2002/07/owl#", "imports"),
                migratedModel.createResource("http://spinrdf.org/spin")),
            "owl:imports <spin> should be removed after second incremental run");
    }

    @Test
    void incrementalNoChangesWhenFullyMigrated() throws IOException {
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
        Path file = createPreformattedFile("already-migrated-incremental.ttl", alreadyMigrated);
        byte[] originalContent = readBytes(file);

        MigrateCLI.main(new String[]{"--incremental", "true", file.toString()});

        assertArrayEquals(originalContent, readBytes(file),
            "Already fully migrated file should not be modified in incremental mode");
    }

    private Path createPreformattedFile(String name, String turtleContent) throws IOException {
        Model model = ModelFactory.createDefaultModel();
        model.read(new ByteArrayInputStream(turtleContent.getBytes(StandardCharsets.UTF_8)), null, FileUtils.langTurtle);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JenaUtils.writeScript(baos, model);
        Path file = tempDir.resolve(name);
        Files.write(file, baos.toByteArray());
        return file;
    }

    private Model parseModel(byte[] bytes) {
        Model model = ModelFactory.createDefaultModel();
        model.read(new ByteArrayInputStream(bytes), null, FileUtils.langTurtle);
        return model;
    }

    private byte[] readBytes(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
