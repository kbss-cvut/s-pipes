package cz.cvut.spipes.engine;

import org.apache.jena.rdf.model.Model;

class ExecutionContextImpl implements ExecutionContext {

    private Model defaultModel;
    private VariablesBinding variablesBinding;

    public Model getDefaultModel() {
        return defaultModel;
    }

    @Override
    public VariablesBinding getVariablesBinding() {
        return variablesBinding;
    }

    @Override
    public String toSimpleString() {

        StringBuilder sb = new StringBuilder();

        sb.append("Context ").append(this.hashCode()).append("[ \n")
                .append("\t varBindings = ").append(variablesBinding).append("\n")
                .append("\t modelSize = ").append(defaultModel.listStatements().toList().size())
                .append("]");

        return sb.toString();
    }

    public void setDefaultModel(Model defaultModel) {
        this.defaultModel = defaultModel;
    }

    public void setVariablesBinding(VariablesBinding variablesBinding) {
        this.variablesBinding = variablesBinding;
    }
}
