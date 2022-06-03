package cz.cvut.sforms.transformation;

import cz.cvut.sforms.model.Question;
import java.util.function.Function;

/**
 * Configuration for transformer from SForms to text.
 */
public class TextTransformerConfig {

    boolean includeAnswer;
    // process only questions that has answers
    boolean requireAnswer;
    boolean includeLabel;
    private Function<Question, String> questionProcessor = Question::getLabel;

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

    public Function<Question, String> getQuestionProcessor() {
        return questionProcessor;
    }

    public void setQuestionProcessor(Function<Question, String> questionProcessor) {
        this.questionProcessor = questionProcessor;
    }

    public boolean isIncludeLabel() {
        return includeLabel;
    }
}
