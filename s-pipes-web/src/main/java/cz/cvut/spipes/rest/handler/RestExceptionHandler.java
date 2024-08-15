package cz.cvut.spipes.rest.handler;

import com.github.jsonldjava.core.JsonLdError;
import cz.cvut.spipes.exception.ValidationConstraintFailedException;
import cz.cvut.spipes.util.RDFMimeType;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

@RestControllerAdvice
public class RestExceptionHandler {
    @ExceptionHandler(ValidationConstraintFailedException.class)
    @ResponseBody
    public ResponseEntity<Object> validationConstraintFailedException(ValidationConstraintFailedException e) throws IOException, JsonLdError {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .contentType(MediaType.parseMediaType(RDFMimeType.LD_JSON_STRING))
            .body(new ErrorValidationResponse(
                e.getModule(),
                e.getErrorMessage(),
                e.getFailedQuery(),
                e.getEvidences()
            ).getFramedAndCompactedJsonLd());
    }
}
