package cz.cvut.spipes.exception;

public class ParseException extends RuntimeException {

    public ParseException() {
        super("Could not parse input string");
    }
}
