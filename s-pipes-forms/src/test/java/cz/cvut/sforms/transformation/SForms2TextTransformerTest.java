package cz.cvut.sforms.transformation;

import cz.cvut.sforms.model.Question;
import cz.cvut.sforms.test.FormGenerator;
import cz.cvut.sforms.util.FormUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNull;


class SForms2TextTransformerTest {

    private TextTransformerConfig cfg;
    private FormGenerator g;
    private SForms2TextTransformer t;

    @BeforeEach
    public void initEach() {
        cfg = new TextTransformerConfig();
        t = new SForms2TextTransformer();
        g = new FormGenerator();
    }

    @Test
    void serializeWithNoSerializeUnansweredQuestionsReturnsNull() {
        Question q = g.questionBuilder()
            .id("root")
            .includeAnswer(false)
            .build();
        cfg.setSerializeUnansweredQuestions(false);

        assertNull(t.serialize(q, cfg));
    }

    @Test
    void serializeByDefaultForLeafQuestionReturnsLabel() {
        Question q = g.createQuestion("root");

        assertThat(t.serialize(q, cfg), containsString(q.getLabel()));
    }

    @Test
    void serializeByDefaultReturnsLabelsOfAllQuestions() {
        Question rootQ = g.questionBuilder()
            .includeAnswer(false)
            .id("1")
            .subQuestion(b -> b.id("1.1")
                .subQuestion(b1 -> b1.id("1.1.1")))
            .subQuestion(b -> b.id("1.2"))
            .build();

        FormUtils.flatten(rootQ).forEach(
            q -> assertThat(t.serialize(q, cfg), containsString(q.getLabel()))
        );
    }

}