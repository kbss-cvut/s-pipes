package cz.cvut.spipes.migration;

import cz.cvut.spipes.util.JenaUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileUtils;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class ReformatCLI {

    @Option(name = "--check-isomorphic-output", usage = "Check that reformatted output is isomorphic with input")
    private boolean checkIsomorphicOutput = true;

    @Argument(required = true, metaVar = "FILES", usage = "One or more files to reformat")
    private List<File> files;

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

        for (File file : cli.files) {
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

                System.out.println("Reformatted: " + file.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Failed to reformat " + file.getAbsolutePath() + ": " + e.getMessage());
            }
        }
    }
}
