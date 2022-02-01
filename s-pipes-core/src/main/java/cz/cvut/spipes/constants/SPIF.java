package cz.cvut.spipes.constants;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class SPIF {

    /**
     * The namespace of the vocabulary as a string
     */
    public static final String uri = "http://spinrdf.org/spif#";

    protected static final Property property(String local )
    { return ResourceFactory.createProperty( uri, local ); }

    /**
     returns the URI for this schema
     @return the URI for this schema
     */
    public static String getURI() {
        return uri;
    }
}
