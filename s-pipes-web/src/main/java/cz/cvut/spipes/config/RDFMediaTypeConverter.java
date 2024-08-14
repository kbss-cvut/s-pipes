package cz.cvut.spipes.config;

import cz.cvut.spipes.util.RDFMimeType;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFLanguages;
import org.slf4j.Logger;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class RDFMediaTypeConverter extends AbstractHttpMessageConverter {

    public RDFMediaTypeConverter() {
        super(
                RDFMimeType.transform(RDFLanguages.N3),
                RDFMimeType.transform(RDFLanguages.NTRIPLES),
                RDFMimeType.transform(RDFLanguages.TURTLE),
                RDFMimeType.transform(RDFLanguages.RDFXML),
                RDFMimeType.transform(RDFLanguages.JSONLD)
        );
    }

    private String getRDFLanguageForContentType( final HttpMessage m, final String defaultValue) {
        log.debug("Getting RDF Language for content type " + m + ", message: " + defaultValue);
        MediaType contentType = m.getHeaders().getContentType();
        if ( contentType == null ) { contentType = MediaType.parseMediaType(defaultValue); }
        return RDFLanguages.contentTypeToLang(contentType.toString().split(";")[0]).getLabel();
    }

    @Override
    protected Object readInternal(Class aClass, HttpInputMessage httpInputMessage) throws IOException, HttpMessageNotReadableException {
        log.debug("Reading " + aClass + ", message: " + httpInputMessage.getHeaders());
        if (  ! aClass.isAssignableFrom( Model.class ) ) {
            throw new UnsupportedOperationException();
        }
        Model inputDataModel = ModelFactory.createDefaultModel();
        inputDataModel.read(httpInputMessage.getBody(), "", getRDFLanguageForContentType(httpInputMessage, RDFMimeType.N_TRIPLES_STRING));
        return inputDataModel;
    }

    @Override
    protected void writeInternal(Object o, HttpOutputMessage httpOutputMessage) throws IOException, HttpMessageNotWritableException {
        if (log.isTraceEnabled()) {
            log.trace("Writing object " + o + ", message: " + httpOutputMessage.getHeaders());
        }
        if (o instanceof Model) {
            log.debug("Writing model of size " + ((Model)o).size() + ", message: " + httpOutputMessage.getHeaders());
        } else {
            throw new UnsupportedOperationException();
        }
        ((Model) o).write(httpOutputMessage.getBody(), getRDFLanguageForContentType(httpOutputMessage, RDFMimeType.LD_JSON_STRING));
    }

    @Override
    protected boolean supports(Class aClass) {
        log.debug("Supports {} ? ", aClass);
        return Model.class.isAssignableFrom(aClass);
    }
}
