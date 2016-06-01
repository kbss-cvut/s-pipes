package cz.cvut.sempipes.service;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AnyMediaTypeConverter extends AbstractHttpMessageConverter {

    public AnyMediaTypeConverter() {
        super(
                RDFMimeType.N_TRIPLES.mediaType,
                RDFMimeType.TURTLE.mediaType,
                RDFMimeType.RDF_XML.mediaType,
                RDFMimeType.LD_JSON.mediaType
        );
    }

    @Override
    protected Object readInternal(Class aClass, HttpInputMessage httpInputMessage) throws IOException, HttpMessageNotReadableException {
        return httpInputMessage.getBody();
    }

    @Override
    protected void writeInternal(Object o, HttpOutputMessage httpOutputMessage) throws IOException, HttpMessageNotWritableException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean supports(Class aClass) {
        return true;
    }
}
