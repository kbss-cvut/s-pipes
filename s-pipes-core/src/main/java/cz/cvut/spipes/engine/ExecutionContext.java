package cz.cvut.spipes.engine;

import org.apache.jena.rdf.model.Model;

public interface ExecutionContext {
    Model getDefaultModel();
    VariablesBinding getVariablesBinding();

    String toSimpleString();


    //getReadOnlyModel();
    //addVariableBinding();
    //addPrefix();
    //clearDefaultModel();

    // TODO named graphs ? -- in merge rather having multiple graphs (for debugging)
    // TODO variable binding supporting stream etc .. ?
    // TODO prefixe map ??
}
