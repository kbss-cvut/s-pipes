package cz.cvut.kbss.util;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public class CmdLineUtils {
    public static void parseCommandLine(String[] args, CmdLineParser argParser) {

        // ---------- arguments parsing ------------
        try {
            argParser.parseArgument(args);

        } catch (CmdLineException ex) {

            System.err.println(ex.getMessage());            
            System.err.print("$0 ");
            argParser.printSingleLineUsage(System.err);            
            argParser.printUsage(System.err);
            System.err.println();
       
            // print option sample. This is useful some time
            //System.err.println("  Example: java SampleMain"+argParser.printExample(ExampleMode.ALL));
            System.exit(1);
        }
    }
}
