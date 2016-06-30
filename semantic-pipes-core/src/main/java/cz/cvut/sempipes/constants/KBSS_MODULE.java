package cz.cvut.sempipes.constants;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Created by Miroslav Blasko on 31.5.16.
 */
public class KBSS_MODULE {

    /**
     * The namespace of the vocabulary as a string
     */
    private static final String uri = "http://onto.fel.cvut.cz/ontologies/lib/module/";

    protected static final Resource resource(String local )
    { return ResourceFactory.createResource( uri + local ); }

    protected static final Property property(String local )
    { return ResourceFactory.createProperty( uri, local ); }

    public static final Resource tarql = resource("tarql");
    public static final Resource form_generator = resource("form-generator");
    public static final Resource deploy = resource("deploy");
    public static final Resource identity = resource("identity");


    public static final Property has_input_graph_constraint = property("has-input-graph-constraint");
    public static final Property has_output_graph_constraint = property("has-output-graph-constraint");

    /**
     returns the URI for this schema
     @return the URI for this schema
     */
    public static String getURI() {
        return uri;
    }
}
