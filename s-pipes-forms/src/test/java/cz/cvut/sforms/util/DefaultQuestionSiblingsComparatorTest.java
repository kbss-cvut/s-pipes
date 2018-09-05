package cz.cvut.sforms.util;


import cz.cvut.sforms.model.Question;
import lombok.val;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DefaultQuestionSiblingsComparatorTest {

    DefaultQuestionSiblingsComparator c;

    @BeforeEach
    void init() {
        c = new DefaultQuestionSiblingsComparator();
    }


    @Test
    public void compareSortsNullsAsLast() {

        val q = new Question();
        val qWithoutLabel = new Question();

        q.setLabel("Q");

        assertEquals(
            new DefaultQuestionSiblingsComparator()
                .compare(q, qWithoutLabel),
            -1);
    }

    @Test
    public void compareSortsByLabels() {

        val q1 = new Question();
        val q2 = new Question();

        q1.setLabel("Q1");
        q2.setLabel("Q2");

        assertEquals(c.compare(q1, q2), -1);
        assertEquals(c.compare(q2, q1), 1);
        assertEquals(c.compare(q1, q1), 0);

    }

    @Test
    public void compareSortsByPrecedingRelation() {

        val q1 = new Question();
        val q2 = new Question();

        q2.getPrecedingQuestions().add(q1);

        assertEquals(c.compare(q1, q2), -1);
        assertEquals(c.compare(q2, q1), 1);
        assertEquals(c.compare(q1, q1), 0);
    }

    @Test
    public void comparePrefersPrecedingRelation() {

        val q1 = new Question();
        val q2 = new Question();

        q1.setLabel("Q1");
        q2.setLabel("Q2");

        Assumptions.assumeTrue(c.compare(q1, q2) < 0);

        q1.getPrecedingQuestions().add(q2);

        assertEquals(c.compare(q1, q2), 1);
        assertEquals(c.compare(q2, q1), -1);
        assertEquals(c.compare(q1, q1), 0);
    }
}
