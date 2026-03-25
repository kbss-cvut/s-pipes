package cz.cvut.spipes.migration.cli;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class MigrateCLI {

    @Option(name = "--only-script-files", handler = ExplicitBooleanOptionHandler.class,
        usage = "Migrate only script files, i.e. files whose owl:import closure contains s-pipes-lib. To resolve import" +
            " closure all relevant files/directories must be specified using 'PATHS'.")
    private boolean onlyScriptFiles = true;

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

        List<File> files = CliFileResolver.resolveFiles(cli.paths, cli.onlyScriptFiles);

        for (File file : files) {
            try {
                Model model = ModelFactory.createDefaultModel();
                model.read(new FileInputStream(file), null, FileUtils.langTurtle);

                new RemoveSpinRdfQueries().apply(model);

                JenaUtils.writeScript(file.toPath(), model);
                System.out.println("Migrated: " + file.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Failed to migrate " + file.getAbsolutePath() + ": " + e.getMessage());
            }
        }
    }
}
