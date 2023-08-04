package cz.cvut.spipes.exception;

public class ParseException extends SPipesException {

    public ParseException() {
        super("Could not parse input string");
    }
}
