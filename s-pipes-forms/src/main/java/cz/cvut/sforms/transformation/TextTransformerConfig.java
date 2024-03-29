package cz.cvut.sforms.transformation;

import cz.cvut.sforms.model.Question;

import java.util.function.Function;

/**
 * Configuration for transformer from SForms to text.
 */
public class TextTransformerConfig {

    boolean isSerializeAnswers = true;
    // process only questions that has answers
    boolean isSerializeUnansweredQuestions = true;
    boolean includeLabel;
    private String indentationString = "  ";
    private Function<Question, String> questionProcessor = Question::getLabel;

    public boolean isSerializeUnansweredQuestions() {
        return isSerializeUnansweredQuestions;
    }

    public void setSerializeUnansweredQuestions(boolean serializeUnansweredQuestions) {
        this.isSerializeUnansweredQuestions = serializeUnansweredQuestions;
    }

    public boolean isSerializeAnswers() {
        return isSerializeAnswers;
    }

    public void setSerializeAnswers(boolean serializeAnswers) {
        this.isSerializeAnswers = serializeAnswers;
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

    public void setIndentationString(String indentationString) {
        this.indentationString = indentationString;
    }

    public String getIndentationString() {
        return indentationString;
    }
}
