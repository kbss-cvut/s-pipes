package cz.cvut.spipes.constants;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class KBSS_CSVW {

    private KBSS_CSVW() {
    }

    /**
     * The namespace of the vocabulary as a string
     */
    public static final String uri = "https://onto.fel.cvut.cz/ontologies/extension/csvw/";

    protected static Resource resource(String local )
    { return ResourceFactory.createResource( uri + local ); }

    protected static Property property(String local )
    { return ResourceFactory.createProperty( uri, local ); }

    public static final String propertyUri = uri + "property";
    public static final String sameValueAsUri = uri + "same-value-as";
    public static final String isPartOfColumn = uri + "is-part-of-column";
    public static final String isPartOfRow = uri + "is-part-of-row";

    public static final Property property = ResourceFactory.createProperty(propertyUri);

    /**
     returns the URI for this schema
     @return the URI for this schema
     */
    public static String getURI() {
        return uri;
    }
}
