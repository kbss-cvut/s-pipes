package cz.cvut.spipes.rest.util;

public class ReservedParams {
    /**
     * Request parameter - 'id' of the module to be executed
     */
    public static final String P_ID = "_pId";
    /**
     * Request parameter - URL of the resource containing configuration,
     * such as execution logging of the s-pipes engine.
     */
    public static final String P_CONFIG_URL = "_pConfigURL";
    /**
     * Input graph - URL of the file where input graph is stored
     */
    public static final String P_INPUT_GRAPH_URL = "_pInputGraphURL";
    /**
     * Input binding - URL of the file where input bindings are stored
     */
    public static final String P_INPUT_BINDING_URL = "_pInputBindingURL";
    /**
     * Output binding - URL of the file where output bindings are stored
     * after execution of module finishes.
     */
    public static final String P_OUTPUT_BINDING_URL = "_pOutputBindingURL";

    /**
     * Context URI - the URI of the context script under which the module identified by P_ID is configured
     */
    public static final String P_CONTEXT_URI = "_pContextURI";
}
