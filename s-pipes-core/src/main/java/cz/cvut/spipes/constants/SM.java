package cz.cvut.spipes.constants;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class SM {

    /**
     * The namespace of the vocabulary as a string
     */
    public static final String uri = "http://topbraid.org/sparqlmotion#";

    protected static final Resource resource(String local )
    { return ResourceFactory.createResource( uri + local ); }

    protected static final Property property(String local )
    { return ResourceFactory.createProperty( uri, local ); }


    public static final Property next = property("next");
    public static final Property outputVariable = property("outputVariable");
    public static final Property returnModule = property("returnModule");

    public static final Resource Function = resource("Function");
    public static final Resource Module = resource("Module");
    public static final Resource Modules = resource("Modules");
}
