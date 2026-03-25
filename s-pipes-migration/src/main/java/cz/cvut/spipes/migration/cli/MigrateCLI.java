package cz.cvut.spipes.migration.cli;

import cz.cvut.spipes.migration.v5.RemoveSpinRdfQueries;
import cz.cvut.spipes.util.JenaUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileUtils;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class MigrateCLI {

    @Argument(required = true, metaVar = "FILES", usage = "One or more files to migrate")
    private List<File> files;

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

        for (File file : cli.files) {
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
