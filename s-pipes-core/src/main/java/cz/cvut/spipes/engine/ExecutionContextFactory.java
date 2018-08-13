package cz.cvut.spipes.engine;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public class ExecutionContextFactory {

    /**
     * @return
     */
    public static ExecutionContext createEmptyContext() {
        ExecutionContextImpl context = new ExecutionContextImpl();
        context.setDefaultModel(ModelFactory.createDefaultModel());
        context.setVariablesBinding(new VariablesBinding());
        return context;
    }

    public static ExecutionContext createContext(Model defaultModel, VariablesBinding variablesBinding) {
        ExecutionContextImpl context = new ExecutionContextImpl();
        context.setDefaultModel(defaultModel);
        context.setVariablesBinding(variablesBinding);
        return context;
    }

    public static ExecutionContext createContext(VariablesBinding variablesBinding) {
        ExecutionContextImpl context = new ExecutionContextImpl();
        context.setDefaultModel(ModelFactory.createDefaultModel());
        context.setVariablesBinding(variablesBinding);
        return context;
    }



    public static ExecutionContext createContext(Model defaultModel) {
        ExecutionContextImpl context = new ExecutionContextImpl();
        context.setDefaultModel(defaultModel);
        context.setVariablesBinding(new VariablesBinding());
        return context;
    }

}
