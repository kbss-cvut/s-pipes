package cz.cvut.sempipes.constants;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Created by Miroslav Blasko on 31.5.16.
 */
public class SPIPES {

    /**
     * The namespace of the vocabulary as a string
     */
    public static final String uri = "http://onto.fel.cvut.cz/ontologies/s-pipes/";

    protected static final org.apache.jena.rdf.model.Resource resource(String local )
    { return ResourceFactory.createResource( uri + local ); }

    protected static final Property property(String local )
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

    public static final Property has_module_type = property("has-module-type");
    public static final Property has_module_id = property("has-module-id");
    public static final Property has_pipeline_input_binding = property("has-pipeline-input-binding");


    /**
     returns the URI for this schema
     @return the URI for this schema
     */
    public static String getURI() {
        return uri;
    }
}
