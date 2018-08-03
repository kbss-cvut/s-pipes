package cz.cvut.sforms.transform;

/**
 * include --
 */
public class TextTransformerConfig {

    boolean includeAnswer;
    boolean requireAnswer;
    boolean includeLabel;

    public boolean isRequireAnswer() {
        return requireAnswer;
    }

    public void setRequireAnswer(boolean requireAnswer) {
        this.requireAnswer = requireAnswer;
    }

    public boolean isIncludeAnswer() {
        return includeAnswer;
    }

    public void setIncludeAnswer(boolean includeAnswer) {
        this.includeAnswer = includeAnswer;
    }
}
