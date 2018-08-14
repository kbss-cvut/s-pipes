package cz.cvut.sforms;

import cz.cvut.sforms.model.Question;
import cz.cvut.sforms.test.FormGenerator;
import java.util.Set;
import static org.hamcrest.MatcherAssert.assertThat;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FormUtilsTest {

    private FormGenerator g;

    @BeforeEach
    public void initEach() {
        g = new FormGenerator();
    }

    @Test
    public void flattenReturnsAllNodesOfQuestionTree() {

        Question rootQ = g.questionBuilder()
            .includeAnswer(false)
            .id("1")
            .subQuestion(b -> b.id("1.1")
                .subQuestion(b1 -> b1.id("1.1.1")))
            .subQuestion(b -> b.id("1.2"))
            .build();

        Set<Question> flattened = FormUtils.flatten(rootQ);

        assertThat( flattened, IsCollectionWithSize.hasSize(4));
    }
}
