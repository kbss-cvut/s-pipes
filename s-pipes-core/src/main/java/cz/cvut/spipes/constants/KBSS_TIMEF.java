package cz.cvut.spipes.constants;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class KBSS_TIMEF {

    /**
     * The namespace of the vocabulary as a string
     */
    public static final String uri = "http://onto.fel.cvut.cz/ontologies/lib/function/time/";

    protected static final Property property(String local )
    { return ResourceFactory.createProperty( uri, local ); }
}
