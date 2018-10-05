package cz.cvut.sforms.analysis;

import cz.cvut.sforms.model.Answer;
import cz.cvut.sforms.model.Question;
import cz.cvut.sforms.util.FormUtils;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

/**
 * Provides partitioning of input forms (given by question roots).
 * Partitions are classes of equivalence.
 * By default equivalence relation is empty, i.e. algorithm computes for each form its own
 * class of equivalence.
 */
public class FormEquivalencePartitioner {

    private List<URI> questionOriginCompositeKey = new LinkedList<>();

    @NotNull
    private static String getQuotedValueByOrigin(Question formRoot, URI questionOrigin) {
        String value = getValueByOrigin(formRoot, questionOrigin);
        if (value == null) {
            return "null";
        } else {
            return "'" + value + "'";
        }
    }

    private static String getValueByOrigin(Question formRoot, URI questionOrigin) {
        return FormUtils.flatten(Stream.of(formRoot))
            .filter(q -> questionOrigin.equals(q.getOrigin()))
            .findFirst()
            .map(FormEquivalencePartitioner::getSingleAnswerValue)
            .orElse(null);
    }

    private static String getSingleAnswerValue(@NotNull Question question) {
        List<String> values = question.getAnswers().stream()
            .map(FormEquivalencePartitioner::getSingleAnswerValue)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        if (values.size() > 1) {
            throw new IllegalArgumentException("Question '" + question + "' contains"
                + " multiple answer values -- '" + Arrays.toString(values.toArray()) + "'.");
        }
        return values.stream().findFirst().orElse(null);
    }

    private static String getSingleAnswerValue(@NotNull Answer answer) {
        List<String> values = Stream.of(
            Optional.ofNullable(answer.getCodeValue()).map(URI::toString),
            Optional.ofNullable(answer.getTextValue())
        )
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());

        if (values.size() > 1) {
            throw new IllegalArgumentException("Answer '" + answer + "' contains code value"
                + " as well as text value -- '" + Arrays.toString(values.toArray()) + "'.");
        }
        return values.stream().findFirst().orElse(null);
    }

    private static long countQuestionsWithOrigin(Question root, URI questionOrigin) {
        return FormUtils.flatten(Stream.of(root)).distinct()
            .filter(q -> questionOrigin.equals(q.getOrigin())).count();
    }

    public void setQuestionOriginCompositeKey(List<URI> questionOriginCompositeKey) {
        this.questionOriginCompositeKey = questionOriginCompositeKey;
    }

    /**
     * Computes partitions over set of forms.
     *
     * @param formRoots list of root form questions
     * @return set of sets of root form questions.
     */
    @NotNull
    public Map<String, Set<Question>> compute(List<Question> formRoots) {

        checkQuestionOriginUniqueness(formRoots);

        return formRoots.stream()
            .collect(Collectors.groupingBy(
                this::computeOriginValuesHash,
                Collectors.toSet()
            ));
    }

    @NotNull
    private String computeOriginValuesHash(Question formRoot) {
        String hash = questionOriginCompositeKey.stream().map(
            qO -> "<" + qO.toString() + "> -> " + getQuotedValueByOrigin(formRoot, qO)
        ).collect(Collectors.joining(", "));

        if (!hash.equals("")) {
            return hash;
        }

        if (formRoot.getUri() == null) {
            throw new UnsupportedOperationException("Not implemented, uri of a question is null.");
        }

        return String.format(
            "unique[%s]",
            formRoot.getUri().toString()
        );
    }

    private void checkQuestionOriginUniqueness(List<Question> formRoots) {
        formRoots.forEach(
            rQ -> questionOriginCompositeKey.forEach(
                qO -> {
                    if (countQuestionsWithOrigin(rQ, qO) > 1) {
                        throw new IllegalArgumentException(
                            "There are more than 1 question with origin '"
                                + qO + "' within a form rooted by '" + rQ + "'.");
                    }
                }
            )
        );
    }


}
