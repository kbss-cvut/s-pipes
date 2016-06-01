package cz.cvut.sempipes.service;

import org.apache.jena.riot.Lang;
import org.springframework.http.MediaType;

import java.nio.charset.Charset;

public enum RDFMimeType {
    TURTLE(new MediaType("text", "turtle", Charset.forName("UTF-8")),Lang.TURTLE),
    RDF_XML(new MediaType("application", "rdf+xml", Charset.forName("UTF-8")), Lang.RDFXML),
    N_TRIPLES(new MediaType("application", "n-triples", Charset.forName("UTF-8")), Lang.NTRIPLES),
    LD_JSON(new MediaType("application", "ld+json", Charset.forName("UTF-8")),Lang.JSONLD);

    public static final String TURTLE_STRING = "text/turtle";
    public static final String RDF_XML_STRING = "application/rdf+xml";
    public static final String N_TRIPLES_STRING = "application/n-triples";
    public static final String LD_JSON_STRING = "application/ld+json";

    public final MediaType mediaType;
    public final Lang lang;

    RDFMimeType(final MediaType mediaType, final Lang lang) {
        this.mediaType = mediaType;
        this.lang = lang;
    }

    public static RDFMimeType forMediaType( final MediaType mediaType ) {
        for(RDFMimeType t : RDFMimeType.values()) {
            if ( t.mediaType.isCompatibleWith(mediaType )) {
                return t;
            }
        }
        return null;
    }

//    text/trig
//    application/n-quads
//    application/trix+xml
//    application/rdf+thrift
}
