package cz.cvut.spipes.engine;

import org.apache.jena.rdf.model.Model;

import java.io.File;
import java.util.List;

/**
 * Execution context containing input model execution. The context is used both as the global execution context of a
 * pipeline as well as the execution context of individual modules in a pipeline.
 *
 * Global context variables are:
 * <li/>_pId - 'id' of the main module to be executed
 * <li/>_pConfigURL - URL of the resource containing configuration, such as execution logging of the s-pipes engine.
 * <li/>_pScriptURI - 'scriptUri' of the execution context identifies the script file used to load module configurations.
 */
public interface ExecutionContext {

    /**
     * Input execution context parameter - 'id' of the main module to be executed
     */
    String ID_PARAM = "_pId";

    /**
     * Input execution context parameter - URL of the resource containing configuration,
     * such as execution logging of the s-pipes engine.
     */
    String P_CONFIG_URL = "_pConfigURL";

    /**
     * Input execution context parameter - 'scriptUri' of the execution context identifies the script file used to load
     * module configurations.
     */
    String P_SCRIPT_URI = "_pScriptURI";

    Model getDefaultModel();
    VariablesBinding getVariablesBinding();

    String toSimpleString();
    String toTruncatedSimpleString();

    /**
     * @param var
     * @return string value bound to <code>var</code>
     */
    String getValue(String var);

    /**
     *
     * @return id of the main module to be executed
     */
    String getId();

    /**
     * Get scriptUri (an ontology iri, file uri or a relative path) which identifies the script for this execution.
     *
     * @return the value of the variable <code>_pScriptURI</code> in this execution context's variablesBindings.
     */
    String getScriptUri();

    /**
     * Bind <code>_pScriptURI</code> to the value of <code>scriptUri</code> in this execution context's variablesBindings.
     * @param scriptUri
     */
    void setScriptUri(String scriptUri);

    /**
     * Get the file corresponding to the value returned by <code>{@link #getScriptUri()}</code>
     * @return
     */
    File getScriptFile();


    //getReadOnlyModel();
    //addVariableBinding();
    //addPrefix();
    //clearDefaultModel();

    // TODO named graphs ? -- in merge rather having multiple graphs (for debugging)
    // TODO variable binding supporting stream etc .. ?
    // TODO prefixe map ??
}
