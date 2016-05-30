package cz.cvut.sempipes.engine;

import org.apache.jena.rdf.model.Model;

/**
 * Created by Miroslav Blasko on 12.5.16.
 */
public class ExecutionContextImpl implements ExecutionContext {

    private Model defaultModel;

    public Model getDefaultModel() {
        return defaultModel;
    }

    public void setDefaultModel(Model defaultModel) {
        this.defaultModel = defaultModel;
    }
}
