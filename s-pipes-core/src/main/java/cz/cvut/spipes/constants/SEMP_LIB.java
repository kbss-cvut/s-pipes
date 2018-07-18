package cz.cvut.spipes.constants;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Created by Miroslav Blasko on 31.5.16.
 */
public class SEMP_LIB {

    /**
     * The namespace of the vocabulary as a string
     */
    private static final String uri = "http://onto.fel.cvut.cz/ontologies/s-pipes/lib/";

    protected static final Resource resource(String local )
    { return ResourceFactory.createResource( uri + local ); }

    protected static final Property property(String local )
    { return ResourceFactory.createProperty( uri, local ); }

    public static final Resource get_pipeline_modules = resource("get-pipeline-modules");

    /**
     returns the URI for this schema
     @return the URI for this schema
     */
    public static String getURI() {
        return uri;
    }
}
