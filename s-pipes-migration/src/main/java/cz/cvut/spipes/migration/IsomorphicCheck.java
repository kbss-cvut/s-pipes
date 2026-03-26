package cz.cvut.spipes.migration;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileUtils;

import java.io.File;
import java.io.FileInputStream;

/**
 * Utility to check if two TTL files are isomorphic.
 * Usage: IsomorphicCheck file1.ttl file2.ttl
 */
public class IsomorphicCheck {

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: IsomorphicCheck file1.ttl file2.ttl");
            System.exit(1);
        }

        Model model1 = ModelFactory.createDefaultModel();
        model1.read(new FileInputStream(new File(args[0])), null, FileUtils.langTurtle);

        Model model2 = ModelFactory.createDefaultModel();
        model2.read(new FileInputStream(new File(args[1])), null, FileUtils.langTurtle);

        if (model1.isIsomorphicWith(model2)) {
            System.out.println("ISOMORPHIC: " + args[0] + " <-> " + args[1]);
        } else {
            System.out.println("NOT ISOMORPHIC: " + args[0] + " <-> " + args[1]);
            System.out.println("  Model 1 size: " + model1.size() + " triples");
            System.out.println("  Model 2 size: " + model2.size() + " triples");
            System.exit(1);
        }
    }
}
