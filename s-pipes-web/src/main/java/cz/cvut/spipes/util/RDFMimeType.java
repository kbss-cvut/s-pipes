package cz.cvut.spipes.util;

import org.apache.jena.riot.Lang;
import org.springframework.http.MediaType;

public class RDFMimeType {
    public static final String TURTLE_STRING = "text/turtle";
    public static final String RDF_XML_STRING = "application/rdf+xml";
    public static final String N_TRIPLES_STRING = "application/n-triples";
    public static final String LD_JSON_STRING = "application/ld+json";

    public static MediaType transform(Lang contentType) {
        return MediaType.parseMediaType(contentType.getContentType().getContentTypeStr());
    }
//    TODO ?
//    text/trig
//    application/n-quads
//    application/trix+xml
//    application/rdf+thrift
}
