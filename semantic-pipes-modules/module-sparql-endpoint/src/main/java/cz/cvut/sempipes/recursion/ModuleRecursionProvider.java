package cz.cvut.sempipes.recursion;

import cz.cvut.sempipes.util.QueryUtils;
import org.apache.jena.rdf.model.Model;

public interface ModuleRecursionProvider {

    boolean shouldTerminate(int currentIteration, Model previousInferredModel, Model currentInferredModel);

}
