package cz.cvut.spipes.constants;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class SPIPES {

    /**
     * The namespace of the vocabulary as a string
     */
    public static final String uri = "http://onto.fel.cvut.cz/ontologies/s-pipes/";

    protected static org.apache.jena.rdf.model.Resource resource(String local)
    { return ResourceFactory.createResource( uri + local ); }

    protected static Property property(String local)
    { return ResourceFactory.createProperty( uri, local ); }

    public static final Resource ProgressListener = resource("progress-listener");

    public static final Property has_classname = property("has-classname");

    public static final Property has_module_execution_start_date = property("has-module-execution-start-date");
    public static final Property has_module_execution_start_date_unix = property("has-module-execution-start-date-unix");
    public static final Property has_module_execution_finish_date = property("has-module-execution-finish-date");
    public static final Property has_module_execution_finish_date_unix = property("has-module-execution-finish-date-unix");
    public static final Property has_module_execution_duration = property("has-module-execution-duration");
    public static final Property has_pipeline_execution_start_date = property("has-pipeline-execution-start-date");
    public static final Property has_pipeline_execution_start_date_unix = property("has-pipeline-execution-start-date-unix");
    public static final Property has_pipeline_execution_finish_date = property("has-pipeline-execution-finish-date");
    public static final Property has_pipeline_execution_finish_date_unix = property("has-pipeline-execution-finish-date-unix");
    public static final Property has_pipeline_execution_duration = property("has-pipeline-execution-duration");
    public static final Property has_input_model_triple_count = property("has-input-model-triple-count");
    public static final Property has_output_model_triple_count = property("has-output-model-triple-count");

    public static final Property has_module_type = property("has-module-type");
    public static final Property has_module_id = property("has-module-id");
    public static final Property has_pipeline_input_binding = property("has-pipeline-input-binding");
    public static final Property has_pipeline_name = property("has-pipeline-name");

    public static final Property has_input_content = property("has-input-content");
    public static final Property has_output_content = property("has-output-content");
    public static final Property has_script = property("has-script");

    public static final Property has_executed_function = property("has-executed-function");
    public static final Property has_executed_function_script_path = property("has-executed-function-script-path");
}
