package cz.cvut.spipes.engine;

import cz.cvut.spipes.config.ContextsConfig;
import cz.cvut.spipes.manager.OntoDocManager;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

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

    public void setScriptUri(String scriptUri) {
        getVariablesBinding().add(
                ExecutionContext.P_SCRIPT_URI,
                getDefaultModel().createResource(scriptUri)
        );
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

    @Override
    public String getValue(String var){
        return Optional.ofNullable(getVariablesBinding().getNode(var))
                .map(RDFNode::toString)
                .orElse(null);
    }

    @Override
    public String getId() {
        return getValue(P_ID);
    }

    @Override
    public String getScriptUri() {
        return getValue(P_SCRIPT_URI);
    }

    /**
     * Get the file corresponding to the value returned by <code>{@link #getScriptUri()}</code>
     * @see OntoDocManager#getScriptFiles(String, List) how scriptUri mapped to a file
     * @return
     */
    @Override
    public File getScriptFile(){
        List<File> files = OntoDocManager.getScriptFiles(getScriptUri(), ContextsConfig.getScriptPaths().stream().map(Path::toString).toList());
        if(files.isEmpty())
            throw new IllegalStateException("Cannot find script file module with id=%s and scriptUri=<%s>.".formatted(getId(), getScriptUri()));
        if(files.size() > 1 )
            throw new IllegalStateException("There are multiple script files found for module with id %s and scriptUri=<%s>.".formatted(getId(), getScriptUri()));

        return files.get(0);
    }
}
