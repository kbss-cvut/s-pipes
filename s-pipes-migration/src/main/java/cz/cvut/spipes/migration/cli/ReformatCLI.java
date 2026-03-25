package cz.cvut.spipes.migration.cli;

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
import java.util.ArrayList;
import java.util.List;

public class ReformatCLI {

    @Option(name = "--check-isomorphic-output", handler = ExplicitBooleanOptionHandler.class,
        usage = "Check that reformatted output is isomorphic with input")
    private boolean checkIsomorphicOutput = true;

    @Option(name = "--only-script-files", handler = ExplicitBooleanOptionHandler.class,
        usage = "Reformat only script files, i.e. files whose owl:import closure contains s-pipes-lib. To resolve import" +
            " closure all relevant files/directories must be specified using 'PATHS'.")
    private boolean onlyScriptFiles = true;

    @Argument(required = true, metaVar = "PATHS", usage = "One or more files or directories to reformat")
    private List<File> paths;

    public static void main(String[] args) {
        ReformatCLI cli = new ReformatCLI();
        CmdLineParser parser = new CmdLineParser(cli);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException ex) {
            System.err.println(ex.getMessage());
            parser.printUsage(System.err);
            System.exit(1);
        }

        CliFileResolver.ResolveResult resolved = CliFileResolver.resolveFiles(cli.paths, cli.onlyScriptFiles);
        List<File> reformattedFiles = new ArrayList<>();

        for (File file : resolved.filesToProcess()) {
            try {
                Model originalModel = ModelFactory.createDefaultModel();
                originalModel.read(new FileInputStream(file), null, FileUtils.langTurtle);

                JenaUtils.writeScript(file.toPath(), originalModel);

                if (cli.checkIsomorphicOutput) {
                    Model rewrittenModel = ModelFactory.createDefaultModel();
                    rewrittenModel.read(new FileInputStream(file), null, FileUtils.langTurtle);
                    if (!originalModel.isIsomorphicWith(rewrittenModel)) {
                        throw new RuntimeException(
                            "Reformatted output is not isomorphic with input for file: " + file.getAbsolutePath()
                        );
                    }
                }

                reformattedFiles.add(file);
            } catch (IOException e) {
                System.err.println("Failed to reformat " + file.getAbsolutePath() + ": " + e.getMessage());
            }
        }

        CliFileResolver.printSummary(resolved.skippedNonScriptFiles(), reformattedFiles, "reformatted");
    }
}
