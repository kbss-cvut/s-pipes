package cz.cvut.spipes.rest.handler;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import cz.cvut.spipes.exception.ValidationConstraintFailedException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

@RestControllerAdvice
public class RestExceptionHandler {
    @ExceptionHandler(ValidationConstraintFailedException.class)
    public ResponseEntity<Object> validationConstraintFailedException(ValidationConstraintFailedException e) throws IOException, JsonLdError {
        return new ResponseEntity<>(new ErrorValidationResponse(
                e.getModule(),
                e.getErrorMessage(),
                e.getFailedQuery(),
                e.getEvidences()
        ).getFramedAndCompactedJsonLd(), HttpStatus.CONFLICT);
    }
}
