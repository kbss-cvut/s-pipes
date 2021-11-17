package cz.cvut.spipes.modules.template;

/**
 * Reports an invalid template.
 */
public class InvalidTemplateException extends Exception {

    InvalidTemplateException(String message) {
        super(message);
    }
}
