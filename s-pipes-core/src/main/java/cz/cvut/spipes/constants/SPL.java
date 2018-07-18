package cz.cvut.spipes.constants;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Created by Miroslav Blasko on 31.5.16.
 */
public class SPL {

    /**
     * The namespace of the vocabulary as a string
     */
    private static final String uri = "http://spinrdf.org/spl#";

    protected static final Resource resource(String local )
    { return ResourceFactory.createResource( uri + local ); }

    protected static final Property property(String local )
    { return ResourceFactory.createProperty( uri, local ); }

    public static final Resource tarql = resource("Argument");

    /**
     returns the URI for this schema
     @return the URI for this schema
     */
    public static String getURI() {
        return uri;
    }
}
