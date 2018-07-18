/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.sempipes.cli;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 *
 * @author blcha
 */
public class UnifiedCLI {

    
    @Argument(required = true, index = 0, metaVar="SUBCOMMAND", usage = "The subcommand of nlp utils ", handler=SubCommandOptionHandler.class)
    private SubCommand subCommand;
    
    public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                                      
        UnifiedCLI asArgs = new UnifiedCLI();
        CmdLineParser argParser = new CmdLineParser(asArgs);
        

        // ---------- arguments parsing ------------
        try {
            argParser.parseArgument(reduceToFirstParam(args));

        } catch (CmdLineException ex) {

            System.err.println(ex.getMessage());
            System.err.println("$0 SUBCOMMAND");
            //argParser.printSingleLineUsage(System.err);
            argParser.printUsage(System.err);
            System.err.println();
            System.err.println("Available subcommands : " + getSubCommands());
            //argParser.printSingleLineUsage(System.err);
            //System.err.println(argParser.printExample(ExampleMode.ALL));
            
            System.err.println();

            // print option sample. This is useful some time
            //System.err.println("  Example: java SampleMain"+argParser.printExample(ExampleMode.ALL));
            System.exit(1);
        }
        
        
        // ---------- sub-command calling ------------
        Method meth = asArgs.subCommand.getAssociatedClass().getMethod("main", String[].class);
        String[] params = shiftParams(args); // init params accordingly
        meth.invoke(null, (Object) params); // static method doesn't have an instance        
    }
    
    private static String[] shiftParams(String[] args) {
        List<String> argsList = new ArrayList(Arrays.asList(args));
        argsList.remove(0);
        return  argsList.toArray(new String[argsList.size()]);
    }
    
    private static String[] reduceToFirstParam(String[] args) {
        if (args.length < 1) {
            return args;
        }
        
        String[] ret = { args[0] };
        return ret;        
    }
    
    
    private static String getSubCommands() {
        StringBuffer rv = new StringBuffer();
    	rv.append("[");
    	for (SubCommand sc : SubCommand.values()) {
			rv.append(sc.toString()).append(" | ");
		}
    	rv.delete(rv.length()-3, rv.length());
    	rv.append("]");
    	return rv.toString();
    }

}
