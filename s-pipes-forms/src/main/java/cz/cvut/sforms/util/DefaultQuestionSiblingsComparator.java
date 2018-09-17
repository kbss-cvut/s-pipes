package cz.cvut.sforms.util;

import cz.cvut.sforms.model.Question;
import java.util.Comparator;

/**
 * Comparator for question siblings that orders them transitively by "preceding question"
 * relation and then lexicographically using labels with "null labels" beeing last.
 *
 * @return comparator.
 */

public class DefaultQuestionSiblingsComparator implements Comparator<Question> {
    @Override
    public int compare(Question q1, Question q2) {
        return
            new TransitiveComparatorPrecedingQuestion()
                .thenComparing(Question::getLabel, Comparator.nullsLast(String::compareTo))
                .compare(q1, q2);
    }
}
