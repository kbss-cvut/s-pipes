package cz.cvut.spipes.modules.constants;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class CSVW {

    private CSVW(){}

    /**
     * The namespace of the vocabulary as a string
     */
    public static final String uri = "http://www.w3.org/ns/csvw#";

    protected static Resource resource(String local )
    { return ResourceFactory.createResource( uri + local ); }

    public static final Resource Row = resource( "Row");
}
