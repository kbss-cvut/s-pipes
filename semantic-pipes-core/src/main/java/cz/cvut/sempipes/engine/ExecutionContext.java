package cz.cvut.sempipes.engine;

import org.apache.jena.rdf.model.Model;

/**
 * Created by blcha on 6.5.16.
 */
public interface ExecutionContext {
    Model getDefaultModel();
    void setDefaultModel(Model model); //TODO rather immutable ?
    //getVariableBinding();

    // TODO named graphs ?
    // TODO variable binding supporting stream etc .. ?
    // TODO prefixe map ??
}
