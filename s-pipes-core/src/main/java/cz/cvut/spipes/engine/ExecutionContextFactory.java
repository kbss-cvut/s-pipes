package cz.cvut.spipes.engine;

import cz.cvut.spipes.manager.factory.ScriptManagerFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import java.util.Optional;

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

    public static ExecutionContext createFunctionContext(Model defaultModel, VariablesBinding variablesBinding) {
        ExecutionContextImpl context = new ExecutionContextImpl();
        context.setDefaultModel(defaultModel);
        context.setVariablesBinding(variablesBinding);
        String id = context.getId();
        String scriptUri = ScriptManagerFactory.getSingletonSPipesScriptManager().getFunctionLocation(id);
        context.setScriptUri(scriptUri);
        return context;
    }

    public static ExecutionContext createModuleContext(Model defaultModel, VariablesBinding variablesBinding, String configuredScriptUri) {
        ExecutionContextImpl context = new ExecutionContextImpl();
        context.setDefaultModel(defaultModel);
        context.setVariablesBinding(variablesBinding);
        String id = context.getId();
        String scriptUri = Optional.ofNullable(context.getScriptUri())
                .map( s ->  ScriptManagerFactory.getSingletonSPipesScriptManager().getModuleLocation(id, s))
                .orElseGet(() -> context.getValue(configuredScriptUri));
        context.setScriptUri(scriptUri);
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
