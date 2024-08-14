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
        return getSimpleString(false);
    }

    @Override
    public String toTruncatedSimpleString() {
        return getSimpleString(true);
    }

    public void setDefaultModel(Model defaultModel) {
        this.defaultModel = defaultModel;
    }

    public void setVariablesBinding(VariablesBinding variablesBinding) {
        this.variablesBinding = variablesBinding;
    }

    private String getSimpleString(boolean truncate) {

        String sb = "Context " + this.hashCode() + "[ \n" +
            "\t varBindings = " + getVariablesBindingString(truncate) + "\n" +
            "\t modelSize = " + defaultModel.size() +
            "]";

        return sb;
    }

    private String getVariablesBindingString(boolean truncate) {
        if (truncate) {
            return variablesBinding.toTruncatedString();
        }
        return variablesBinding.toString();
    }
}
