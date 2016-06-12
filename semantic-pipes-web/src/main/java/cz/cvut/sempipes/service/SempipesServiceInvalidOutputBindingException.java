package cz.cvut.sempipes.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST, reason="Invalid/No output binding URL supplied. Currently only file: URLs are supported.")
public class SempipesServiceInvalidOutputBindingException extends RuntimeException {
    public SempipesServiceInvalidOutputBindingException() {
    }

    public SempipesServiceInvalidOutputBindingException(Throwable cause) {
        super(cause);
    }
}
