package cz.cvut.sempipes.modules;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;


public class MySUTime {
    /**
     * The namespace of the vocabulary as a string
     */
    private static final String uri = SUTime2Module.TYPE_URI;

    protected static final Resource resource(String local )
    { return ResourceFactory.createResource( uri + local ); }

    protected static final Property property(String local )
    { return ResourceFactory.createProperty( uri, local ); }

    public static final Resource sutime_extraction = resource("sutime-extraction");

    public static final Property has_document_date = property("has-document-date");
    public static final Property has_rule_file = property("has-rule-file");

    public static final Property beginDate = property("beginDate");
    public static final Property endDate = property("endDate");
    public static final Property type = property("type");
    public static final Property extracted = property("extracted");
    /**
     returns the URI for this schema
     @return the URI for this schema
     */
    public static String getURI() {
        return uri;
    }
}
