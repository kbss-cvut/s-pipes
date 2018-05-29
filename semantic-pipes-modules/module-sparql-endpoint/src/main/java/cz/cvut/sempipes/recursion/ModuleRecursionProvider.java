package cz.cvut.sempipes.recursion;

import org.apache.jena.rdf.model.Model;

public interface ModuleRecursionProvider {

    boolean shouldTerminate(int currentIteration, Model previousInferredModel, Model currentInferredModel);

}
