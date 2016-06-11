package cz.cvut.sempipes.engine;

import org.apache.jena.rdf.model.Model;

import java.util.Map;

/**
 *
 * Created by blcha on 6.5.16.
 */
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
