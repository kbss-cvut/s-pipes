package cz.cvut.sforms.transform;

import cz.cvut.sforms.model.Question;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SForms2TextTransformerTest {

    TextTransformerConfig cfg;

    @BeforeEach
    public void initEach() {
        cfg = new TextTransformerConfig();
    }

    @Test
    public void serializeForQuestionWithoutRequiredAnswerReturnsNull() {
        Question q = new Question();


        cfg.setRequireAnswer(true);

        SForms2TextTransformer transformer = new SForms2TextTransformer();

        assertNull(transformer.serialize(q, cfg));
    }

    @Test
    public void serializeForLeafQuestionReturnsLabel() {
        Question q = new Question();

    }
}