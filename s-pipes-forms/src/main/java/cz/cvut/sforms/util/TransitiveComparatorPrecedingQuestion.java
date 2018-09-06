package cz.cvut.sforms.util;

import cz.cvut.sforms.model.Question;
import java.util.Comparator;

/**
 * Ineffective implementation of comparator that compare questions based on
 * <code>Question.hasPrecedingQuestion</code> method interpreting it transitively.
 */
class TransitiveComparatorPrecedingQuestion implements Comparator<Question> {

    @Override
    public int compare(Question q1, Question q2) {
        if (hasPrecedingQuestion(q2, q1)) {
            return -1;
        }
        if (hasPrecedingQuestion(q1, q2)) {
            return 1;
        }
        return 0;
    }

    private boolean hasPrecedingQuestion(final Question q1, final Question q2) {
        if (q1.getPrecedingQuestions().contains(q2)) {
            return true;
        }
        return q1.getPrecedingQuestions().stream()
            .anyMatch(q -> hasPrecedingQuestion(q, q2));
    }


}
