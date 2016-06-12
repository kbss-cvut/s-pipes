package cz.cvut.sempipes.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST, reason="Invalid/No config URL supplied.")
public class SempipesServiceInvalidConfigurationException extends RuntimeException {}
