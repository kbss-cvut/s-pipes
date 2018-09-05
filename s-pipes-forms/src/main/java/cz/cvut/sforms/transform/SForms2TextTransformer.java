package cz.cvut.sforms.transform;

import cz.cvut.sforms.model.Question;
import cz.cvut.sforms.util.DefaultQuestionSiblingsComparator;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SForms2TextTransformer {

    private static final String INDENTATION = " ";

    static String appendPrefixToEachLine(String prefix, String s) {
        String ret = prefix + s.replaceAll("(\r\n|\n)", "$1" + prefix);
        return ret.substring(0, ret.length() - prefix.length());
    }

    @Nullable
    public String serialize(@NotNull Question question, @Nullable TextTransformerConfig cfg) {

        return serializeRecursive(question, cfg);
    }

    @Nullable
    private String serializeRecursive(Question question, TextTransformerConfig cfg) {

        if (cfg.isRequireAnswer() && question.getAnswers().isEmpty()) {
            return null;
        }

        String subQStr = question.getSubQuestions().stream()
            .sorted(new DefaultQuestionSiblingsComparator())
            .map(sq -> serializeRecursive(sq, cfg))
            .filter(Objects::nonNull)
            .map(sq -> appendPrefixToEachLine(INDENTATION, sq))
            .collect(Collectors.joining());

        String qStr = cfg.getQuestionProcessor().apply(question);

        StringBuffer sb = new StringBuffer();
        sb
            .append(qStr)
            .append("\n")
            .append(subQStr);

        return sb.toString();
    }


}
