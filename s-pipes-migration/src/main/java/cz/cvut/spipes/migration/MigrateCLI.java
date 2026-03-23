package cz.cvut.spipes.migration;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.File;
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
            System.err.println("Usage: migrate FILE [FILE...]");
            parser.printUsage(System.err);
            System.exit(1);
        }

        for (File file : cli.files) {
            System.out.println("Migrating file: " + file.getAbsolutePath());
            // TODO implement migration logic
        }
    }
}
