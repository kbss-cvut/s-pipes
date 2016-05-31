package cz.cvut.sempipes.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST, reason="No module id supplied.")
public class SempipesServiceNoModuleIdException extends RuntimeException {}
