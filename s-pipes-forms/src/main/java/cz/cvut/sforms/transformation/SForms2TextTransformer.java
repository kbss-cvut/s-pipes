package cz.cvut.sforms.transformation;

import cz.cvut.sforms.model.Question;
import cz.cvut.sforms.util.DefaultQuestionSiblingsComparator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.stream.Collectors;

public class SForms2TextTransformer {


    static String appendPrefixToEachLine(String prefix, String s) {
        String ret = prefix + s.replaceAll("(\r\n|\n)", "$1" + prefix);
        return ret.substring(0, ret.length() - prefix.length());
    }

    @Nullable
    public String serialize(@NotNull Question question, @NotNull TextTransformerConfig cfg) {

        return serializeRecursive(question, cfg);
    }

    @Nullable
    private String serializeRecursive(@NotNull Question question, @NotNull TextTransformerConfig cfg) {

        if ((! cfg.isSerializeUnansweredQuestions()) && question.getAnswers().isEmpty()) {
            return null;
        }

        String subQStr = question.getSubQuestions().stream()
            .sorted(new DefaultQuestionSiblingsComparator())
            .map(sq -> serializeRecursive(sq, cfg))
            .filter(Objects::nonNull)
            .map(sq -> appendPrefixToEachLine(cfg.getIndentationString(), sq))
            .collect(Collectors.joining());

        String qStr = cfg.getQuestionProcessor().apply(question);

        String sb = qStr +
            "\n" +
            subQStr;

        return sb;
    }


}
