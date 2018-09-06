package cz.cvut.sforms.util;

import cz.cvut.sforms.model.Question;
import java.util.Comparator;

/**
 *  Implementation of comparator that compare questions based on
 *  <code>Question.hasPrecedingQuestion</code> method.
 *  In order for the comparator to work correctly the method must
 *  be reflecting transitive relation.
 */
public class PrecedingQuestionComparator implements Comparator<Question> {

    @Override
    public int compare(Question q1, Question q2) {
        if (q2.getPrecedingQuestions().contains(q1)) {
            return -1;
        }
        if (q1.getPrecedingQuestions().contains(q2)) {
            return 1;
        }
        return 0;
    }
}
