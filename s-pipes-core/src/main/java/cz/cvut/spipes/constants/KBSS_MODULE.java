package cz.cvut.spipes.constants;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Created by Miroslav Blasko on 31.5.16.
 */
public class KBSS_MODULE {

    /**
     * The namespace of the vocabulary as a string
     */
    public static final String uri = "http://onto.fel.cvut.cz/ontologies/lib/module/";

    protected static final Property property(String local )
    { return ResourceFactory.createProperty( uri, local ); }

    public static final Property has_input_graph_constraint = property("has-input-graph-constraint");
    public static final Property has_output_graph_constraint = property("has-output-graph-constraint");
    public static final Property has_target_module_flag = property("has-target-module-flag");
    public static final Property has_debug_mode_flag = property("has-debug-mode-flag");
    public static final Property is_parse_text = property("is-parse-text");
    public static final Property has_max_iteration_count = property("has-max-iteration-count");

    /**
     returns the URI for this schema
     @return the URI for this schema
     */
    public static String getURI() {
        return uri;
    }
}
