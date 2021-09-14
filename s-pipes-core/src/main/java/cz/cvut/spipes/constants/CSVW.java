package cz.cvut.spipes.constants;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class CSVW {

    /**
     * The namespace of the vocabulary as a string
     */
    public static final String uri = "http://www.w3.org/ns/csvw#";

    protected static final Resource resource(String local )
    { return ResourceFactory.createResource( uri + local ); }

    protected static final Property property(String local )
    { return ResourceFactory.createProperty( uri, local ); }

    public static final Property table = property( "table");
    public static final Property url = property( "url");
    public static final Property row = property( "row");
    public static final Property rowNum = property( "rownum");
    public static final Property describes = property( "describes");
    public static final Property hasAboutUrl = property("aboutUrl");
    public static final Property hasPropertyUrl = property("propertyUrl");
    public static final Property hasName = property("name");
    public static final Property hasValueUrl = property("valueUrl");

    public static final Resource TableGroup = resource("TableGroup");
    public static final Resource Table = resource("Table");
    public static final Resource Row = resource("Row");

    /**
     returns the URI for this schema
     @return the URI for this schema
     */
    public static String getURI() {
        return uri;
    }
}
