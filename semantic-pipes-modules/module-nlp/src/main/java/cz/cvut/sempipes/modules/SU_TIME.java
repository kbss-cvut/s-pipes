package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.KBSS_MODULE;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Created by Miroslav Blasko on 31.5.16.
 */
public class SU_TIME {

    /**
     * The namespace of the vocabulary as a string
     */
    private static final String uri = KBSS_MODULE.su_time.getURI() + "/";

    protected static final Resource resource(String local )
    { return ResourceFactory.createResource( uri + local ); }

    protected static final Property property(String local )
    { return ResourceFactory.createProperty( uri, local ); }

    public static final Resource sutime_extraction = resource("sutime-extraction");

    public static final Property has_document_date = property("has-document-date");
    public static final Property has_rule_file = property("has-rule-file");

    public static final Property has_character_offset_begin = property("has-character-offset-begin");
    public static final Property has_character_offset_end = property("has-character-offset-end");
    public static final Property has_temporal_expression = property("has-temporal-expression");
    public static final Property has_core_map = property("has-core-map");

    /**
     returns the URI for this schema
     @return the URI for this schema
     */
    public static String getURI() {
        return uri;
    }
}
