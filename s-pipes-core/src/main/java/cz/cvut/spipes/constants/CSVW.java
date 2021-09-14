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

    public static final Property hasTable = property( "table");
    public static final Property hasUrl = property( "url");
    public static final Property hasRow = property( "row");
    public static final Property hasRowNum = property( "rownum");
    public static final Property hasDescribes = property( "describes");

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