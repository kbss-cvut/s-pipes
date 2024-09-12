package cz.cvut.spipes.constants;

import org.apache.jena.rdf.model.Property;

import static org.apache.jena.rdf.model.ResourceFactory.createProperty;

public class KBSS_MODULE {

    /**
     * The namespace of the vocabulary as a string
     */
    public static final String uri = "http://onto.fel.cvut.cz/ontologies/lib/module/";

    public static final String has_input_graph_constraint = uri + "has-input-graph-constraint";
    public static final String has_output_graph_constraint = uri + "has-output-graph-constraint";
    public static final String has_target_module_flag = uri + "has-target-module-flag";
    public static final String has_debug_mode_flag = uri + "has-debug-mode-flag";
    public static final String is_parse_text = uri + "is-parse-text";
    public static final String has_max_iteration_count = uri + "has-max-iteration-count";
    public static final String has_resource_uri = uri + "has-resource-uri";

    // states that reified statement belongs to specific named graph identified by uri
    public static final String is_part_of_graph = uri + "is-part-of-graph";

    public static class JENA {
        public static final Property has_input_graph_constraint = createProperty(KBSS_MODULE.has_input_graph_constraint);
        public static final Property has_output_graph_constraint = createProperty(KBSS_MODULE.has_output_graph_constraint);
        public static final Property has_target_module_flag = createProperty(KBSS_MODULE.has_target_module_flag);
        public static final Property has_debug_mode_flag = createProperty(KBSS_MODULE.has_debug_mode_flag);
        public static final Property is_parse_text = createProperty(KBSS_MODULE.is_parse_text);
        public static final Property has_max_iteration_count = createProperty(KBSS_MODULE.has_max_iteration_count);
        public static final Property has_resource_uri = createProperty(KBSS_MODULE.has_resource_uri);
        public static final Property is_part_of_graph = createProperty(KBSS_MODULE.is_part_of_graph);
    }
}
