package cz.cvut.spipes.constants;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class KM_PARAM {

    /**
     * The namespace of the vocabulary as a string
     */
    public static final String uri = "http://onto.fel.cvut.cz/ontologies/lib/module-param/";

    protected static final Property property(String local )
    { return ResourceFactory.createProperty( uri, local ); }

    public static final Property has_resource_uri = property("has-resource-uri");


    /**
     returns the URI for this schema
     @return the URI for this schema
     */
    public static String getURI() {
        return uri;
    }
}
