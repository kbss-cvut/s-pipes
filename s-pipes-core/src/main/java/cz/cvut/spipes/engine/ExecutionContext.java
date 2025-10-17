package cz.cvut.spipes.engine;

import org.apache.jena.rdf.model.Model;

import java.io.File;

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
     * Input execution context parameter - 'id' of a function in case of pipeline execution or 'id' of a module in case
     * of module execution use-case. The id can be either URI or a localname of the URI in case it is unique in this
     * execution context.
     */
    String P_ID = "_pId";

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
     * Get id of a function in case of pipeline execution, or 'id' of a module in case of the module execution use-case.
     *
     * @return the value of the variable <code>_pId</code> from the variables bindings of this execution context.
     */
    String getId();

    /**
     * Get scriptUri (an ontology iri, file uri or a relative path) which identifies the script for this execution.
     *
     * @return the value of the variable <code>_pScriptURI</code> from the variables bindings of this execution context.
     */
    String getScriptUri();

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
    // TODO prefix map ??
}
