package cz.cvut.sempipes.config;

import cz.cvut.sempipes.util.RDFMimeType;
import org.apache.jena.riot.RDFLanguages;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;

import java.io.IOException;

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
