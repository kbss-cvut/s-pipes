package cz.cvut.spipes.migration.cli;

import cz.cvut.spipes.migration.v5.RefactorSpinExpressions;
import cz.cvut.spipes.migration.v5.RefactorSpinFunctionsToShacl;
import cz.cvut.spipes.migration.v5.RemoveSpinRdfQueries;
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
import java.util.List;

public class MigrateCLI {

    @Option(name = "--only-script-files", handler = ExplicitBooleanOptionHandler.class,
        usage = "Migrate only script files, i.e. files whose owl:import closure contains s-pipes-lib. To resolve import" +
            " closure all relevant files/directories must be specified using 'PATHS'.")
    private boolean onlyScriptFiles = true;

    @Option(name = "--ensure-formatted-input", handler = ExplicitBooleanOptionHandler.class,
        usage = "Skip files that are not preformatted. Use 'reformat' command to preformat them first.")
    private boolean ensureFormattedInput = true;

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

                new RemoveSpinRdfQueries().apply(model);
                new RefactorSpinFunctionsToShacl().apply(model);
                new RefactorSpinExpressions().apply(model);

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
}
