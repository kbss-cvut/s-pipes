package cz.cvut.spipes.constants;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

// TODO - delete?
public class SPL {

    /**
     * The namespace of the vocabulary as a string
     */
    private static final String uri = "http://spinrdf.org/spl#";

    protected static Resource resource(String local)
    { return ResourceFactory.createResource( uri + local ); }

    protected static Property property(String local)
    { return ResourceFactory.createProperty( uri, local ); }

    public static final Resource tarql = resource("Argument");
}
