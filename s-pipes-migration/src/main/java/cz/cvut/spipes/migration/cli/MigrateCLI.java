package cz.cvut.spipes.migration.cli;

import cz.cvut.spipes.migration.v5.RefactorSpinExpressions;
import cz.cvut.spipes.migration.v5.RefactorSpinFunctionsToShacl;
import cz.cvut.spipes.migration.v5.RemoveSpinRdfQueries;
import cz.cvut.spipes.migration.v5.UpdatePrefixesAndImports;
import cz.cvut.spipes.util.JenaUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileUtils;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.ExplicitBooleanOptionHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MigrateCLI {

    record MigrationStep(String name, Consumer<Model> action) {}

    private static final List<MigrationStep> MIGRATION_STEPS = List.of(
        new MigrationStep("RemoveSpinRdfQueries", m -> new RemoveSpinRdfQueries().apply(m)),
        new MigrationStep("RefactorSpinFunctionsToShacl", m -> new RefactorSpinFunctionsToShacl().apply(m)),
        new MigrationStep("RefactorSpinExpressions", m -> new RefactorSpinExpressions().apply(m)),
        new MigrationStep("UpdatePrefixesAndImports", m -> new UpdatePrefixesAndImports().apply(m))
    );

    @Option(name = "--only-script-files", handler = ExplicitBooleanOptionHandler.class,
        usage = "Migrate only script files, i.e. files whose owl:import closure contains s-pipes-lib. To resolve import" +
            " closure all relevant files/directories must be specified using 'PATHS'.")
    private boolean onlyScriptFiles = true;

    @Option(name = "--ensure-formatted-input", handler = ExplicitBooleanOptionHandler.class,
        usage = "Skip files that are not preformatted. Use 'reformat' command to preformat them first.")
    private boolean ensureFormattedInput = true;

    @Option(name = "--incremental", handler = ExplicitBooleanOptionHandler.class,
        usage = "Apply only minimalistic migration step that causes changes to at least one file. " +
            "Run repeatedly to progress through all steps.")
    private boolean incremental = false;

    @Argument(required = true, metaVar = "PATHS", usage = "One or more files or directories to migrate")
    private List<File> paths;

    public static void main(String[] args) {
        MigrateCLI cli = new MigrateCLI();
        CmdLineParser parser = new CmdLineParser(cli);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException ex) {
            System.err.println(ex.getMessage());
            parser.printUsage(System.err);
            System.exit(1);
        }

        CliFileResolver.ResolveResult resolved = CliFileResolver.resolveFiles(cli.paths, cli.onlyScriptFiles);

        if (cli.incremental) {
            runIncrementalMigration(cli, resolved);
        } else {
            runFullMigration(cli, resolved);
        }
    }

    private static void runFullMigration(MigrateCLI cli, CliFileResolver.ResolveResult resolved) {
        List<File> migratedFiles = new ArrayList<>();
        List<File> alreadyMigratedFiles = new ArrayList<>();
        List<File> skippedNotPreformatted = new ArrayList<>();
        List<File> failedFiles = new ArrayList<>();

        for (File file : resolved.filesToProcess()) {
            try {
                byte[] originalBytes = Files.readAllBytes(file.toPath());

                Model model = ModelFactory.createDefaultModel();
                model.read(new ByteArrayInputStream(originalBytes), null, FileUtils.langTurtle);

                if (cli.ensureFormattedInput) {
                    ByteArrayOutputStream reformatBaos = new ByteArrayOutputStream();
                    JenaUtils.writeScript(reformatBaos, model);
                    if (!Arrays.equals(originalBytes, reformatBaos.toByteArray())) {
                        skippedNotPreformatted.add(file);
                        continue;
                    }
                }

                for (MigrationStep step : MIGRATION_STEPS) {
                    step.action().accept(model);
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                JenaUtils.writeScript(baos, model);
                byte[] migratedBytes = baos.toByteArray();

                if (Arrays.equals(originalBytes, migratedBytes)) {
                    alreadyMigratedFiles.add(file);
                    continue;
                }

                Files.write(file.toPath(), migratedBytes);
                migratedFiles.add(file);
            } catch (Exception e) {
                System.err.println("Failed to migrate " + file.getAbsolutePath() + ": " + e.getMessage());
                failedFiles.add(file);
            }
        }

        CliFileResolver.printSummary(resolved.skippedNonScriptFiles(), alreadyMigratedFiles,
            skippedNotPreformatted, failedFiles, migratedFiles, "migrated");
    }

    private static void runIncrementalMigration(MigrateCLI cli, CliFileResolver.ResolveResult resolved) {
        List<File> skippedNotPreformatted = new ArrayList<>();
        List<File> failedFiles = new ArrayList<>();

        // Phase 1: Pre-check formatting and build eligible file list
        List<File> eligibleFiles = new ArrayList<>();
        Map<File, byte[]> originalBytesCache = new LinkedHashMap<>();

        for (File file : resolved.filesToProcess()) {
            try {
                byte[] originalBytes = Files.readAllBytes(file.toPath());

                if (cli.ensureFormattedInput) {
                    Model model = ModelFactory.createDefaultModel();
                    model.read(new ByteArrayInputStream(originalBytes), null, FileUtils.langTurtle);
                    ByteArrayOutputStream reformatBaos = new ByteArrayOutputStream();
                    JenaUtils.writeScript(reformatBaos, model);
                    if (!Arrays.equals(originalBytes, reformatBaos.toByteArray())) {
                        skippedNotPreformatted.add(file);
                        continue;
                    }
                }

                eligibleFiles.add(file);
                originalBytesCache.put(file, originalBytes);
            } catch (Exception e) {
                System.err.println("Failed to read " + file.getAbsolutePath() + ": " + e.getMessage());
                failedFiles.add(file);
            }
        }

        // Phase 2: Try each step incrementally
        for (MigrationStep step : MIGRATION_STEPS) {
            List<File> changedByThisStep = new ArrayList<>();
            Map<File, byte[]> pendingWrites = new LinkedHashMap<>();

            for (File file : eligibleFiles) {
                try {
                    byte[] originalBytes = originalBytesCache.get(file);
                    Model model = ModelFactory.createDefaultModel();
                    model.read(new ByteArrayInputStream(originalBytes), null, FileUtils.langTurtle);

                    step.action().accept(model);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    JenaUtils.writeScript(baos, model);
                    byte[] migratedBytes = baos.toByteArray();

                    if (!Arrays.equals(originalBytes, migratedBytes)) {
                        changedByThisStep.add(file);
                        pendingWrites.put(file, migratedBytes);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to apply step '" + step.name()
                        + "' to " + file.getAbsolutePath() + ": " + e.getMessage());
                    failedFiles.add(file);
                }
            }

            if (!changedByThisStep.isEmpty()) {
                // Write all changed files
                for (Map.Entry<File, byte[]> entry : pendingWrites.entrySet()) {
                    try {
                        Files.write(entry.getKey().toPath(), entry.getValue());
                    } catch (IOException e) {
                        System.err.println("Failed to write " + entry.getKey().getAbsolutePath()
                            + ": " + e.getMessage());
                        failedFiles.add(entry.getKey());
                        changedByThisStep.remove(entry.getKey());
                    }
                }

                List<File> unchangedByThisStep = new ArrayList<>(eligibleFiles);
                unchangedByThisStep.removeAll(changedByThisStep);
                unchangedByThisStep.removeAll(failedFiles);

                System.out.println("Applied incremental migration step: " + step.name());
                CliFileResolver.printSummary(resolved.skippedNonScriptFiles(), unchangedByThisStep,
                    skippedNotPreformatted, failedFiles, changedByThisStep,
                    "migrated (step: " + step.name() + ")");
                return;
            }
        }

        // No step caused any changes
        System.out.println("All files are already fully migrated.");
        CliFileResolver.printSummary(resolved.skippedNonScriptFiles(), eligibleFiles,
            skippedNotPreformatted, failedFiles, List.of(), "migrated");
    }
}
