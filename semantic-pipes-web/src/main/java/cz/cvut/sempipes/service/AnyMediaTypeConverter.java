package cz.cvut.sempipes.service;

import org.apache.jena.riot.RDFLanguages;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AnyMediaTypeConverter extends AbstractHttpMessageConverter {

    public AnyMediaTypeConverter() {
        super(
                MediaType.parseMediaType(RDFLanguages.N3.getContentType().getContentType()),
                MediaType.parseMediaType(RDFLanguages.NTRIPLES.getContentType().getContentType()),
                MediaType.parseMediaType(RDFLanguages.TURTLE.getContentType().getContentType()),
                MediaType.parseMediaType(RDFLanguages.RDFXML.getContentType().getContentType()),
                MediaType.parseMediaType(RDFLanguages.JSONLD.getContentType().getContentType())
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
