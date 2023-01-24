package cz.cvut.spipes.engine;

import cz.cvut.spipes.util.CoreConfigProperies;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;

import java.util.Map;

public class ExecutionContextFactory {

    /**
     * @return
     */
    public static ExecutionContext createEmptyContext() {
        ExecutionContextImpl context = new ExecutionContextImpl();
        context.setDefaultModel(ModelFactory.createDefaultModel());
        context.setVariablesBinding(extendByConfigurationVariables(new VariablesBinding()));
        return context;
    }

    public static ExecutionContext createContext(Model defaultModel, VariablesBinding variablesBinding) {
        ExecutionContextImpl context = new ExecutionContextImpl();
        context.setDefaultModel(defaultModel);
        context.setVariablesBinding(extendByConfigurationVariables(variablesBinding));
        return context;
    }

    public static ExecutionContext createContext(VariablesBinding variablesBinding) {
        ExecutionContextImpl context = new ExecutionContextImpl();
        context.setDefaultModel(ModelFactory.createDefaultModel());
        context.setVariablesBinding(extendByConfigurationVariables(variablesBinding));
        return context;
    }



    public static ExecutionContext createContext(Model defaultModel) {
        ExecutionContextImpl context = new ExecutionContextImpl();
        context.setDefaultModel(defaultModel);
        context.setVariablesBinding(extendByConfigurationVariables(new VariablesBinding()));
        return context;
    }

    private static VariablesBinding extendByConfigurationVariables(VariablesBinding variable) {
        Map<String,String> map = CoreConfigProperies.getConfigurationVariables();
        variable.extendConsistently(new VariablesBinding(transform(map)));
        return variable;
    }

    private static QuerySolution transform(final Map<String,String> parameters) {
        final QuerySolutionMap querySolution = new QuerySolutionMap();

        parameters.entrySet().forEach(
                e -> querySolution.add(e.getKey(), ResourceFactory.createPlainLiteral(e.getValue()))
        );
        return querySolution;
    }
}
