package cz.cvut.sforms.analysis;


import cz.cvut.sforms.model.Question;
import cz.cvut.sforms.test.FormGenerator;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FormEquivalencePartitionerTest {

    FormEquivalencePartitioner formEquivalence;
    private FormGenerator g;

    @BeforeEach
    private void initEach() {
        g = new FormGenerator();
        formEquivalence = new FormEquivalencePartitioner();
    }


    @Test
    public void computeByDefaultReturnsSingletons() {
        List<Question> forms = createRandomForms(3);

        Collection<Set<Question>> eqClasses = formEquivalence.compute(
            forms
        ).values();

        assertEquals(3, eqClasses.size());
        eqClasses.forEach(
            eqCls -> assertEquals(eqCls.size(), 1)
        );
    }

    @Test
    public void computeFailsIfQuestionOriginsAreNotUnique() {
        URI qOrigin = URI.create("http://example.cz/quesiton-origin");
        formEquivalence.setQuestionOriginCompositeKey(Collections.singletonList(qOrigin));

        Question q1 = g.questionBuilder()
            .id("1")
            .subQuestion(b -> b.id("1.1").origin(qOrigin))
            .build();
        Question q2 = g.questionBuilder()
            .id("2").origin(qOrigin)
            .subQuestion(b -> b.id("2.1").origin(qOrigin))
            .subQuestion(b -> b.id("2.2"))
            .build();

        assertThrows(
            IllegalArgumentException.class,
            () -> {
                formEquivalence.compute(
                    Stream.of(q1, q2).collect(Collectors.toList())
                );
            }
        );
    }

    @Test
    public void computeWithSameOriginBasedAnswerValuesReturnsSamePartition() {
        URI qOrigin = URI.create("http://example.cz/quesiton-origin");
        String SAMPLE_VALUE = "sample-value";
        formEquivalence.setQuestionOriginCompositeKey(Collections.singletonList(qOrigin));

        Question q1 = g.questionBuilder()
            .id("1")
            .subQuestion(b -> b.id("1.1").origin(qOrigin)
                .answer(a -> a.textValue(SAMPLE_VALUE)))
            .build();
        Question q2 = g.questionBuilder()
            .id("2")
            .subQuestion(b -> b.id("2.1").origin(qOrigin)
                .answer(a -> a.textValue(SAMPLE_VALUE))
            )
            .subQuestion(b -> b.id("2.2"))
            .build();

        Map<String, Set<Question>> eqClasses = formEquivalence.compute(
            Stream.of(q1, q2).collect(Collectors.toList())
        );

        assertEquals(1, eqClasses.size());
    }

    @Test
    public void computeWithDifferentOriginBasedAnswerValuesReturnsDifferentPartitions() {
        URI qOrigin = URI.create("http://example.cz/quesiton-origin");

        formEquivalence.setQuestionOriginCompositeKey(Collections.singletonList(qOrigin));

        Question q1 = g.questionBuilder()
            .id("1")
            .subQuestion(b -> b.id("1.1").origin(qOrigin)
                .answer(a -> a.textValue("a value")))
            .build();
        Question q2 = g.questionBuilder()
            .id("2")
            .subQuestion(b -> b.id("2.1").origin(qOrigin)
                .answer(a -> a.textValue("different value"))
            )
            .subQuestion(b -> b.id("2.2"))
            .build();

        Map<String, Set<Question>> eqClasses = formEquivalence.compute(
            Stream.of(q1, q2).collect(Collectors.toList())
        );

        assertEquals(2, eqClasses.size());
    }

    @Test
    public void computeWithMultipleOriginsAndSameValuesReturnsSamePartition() {
        URI qOrigin1 = URI.create("http://example.cz/quesiton-origin-1");
        URI qOrigin2 = URI.create("http://example.cz/quesiton-origin-2");
        String SAMPLE_VALUE_1 = "sample-value-1";
        String SAMPLE_VALUE_2 = "sample-value-2";
        formEquivalence.setQuestionOriginCompositeKey(Arrays.asList(qOrigin1, qOrigin2));

        Question q1 = g.questionBuilder()
            .id("1")
            .subQuestion(b -> b.id("1.1").origin(qOrigin1)
                .answer(a -> a.textValue(SAMPLE_VALUE_1))
            )
            .subQuestion(b -> b.id("1.2").origin(qOrigin2)
                .answer(a -> a.textValue(SAMPLE_VALUE_2))
            )
            .build();
        Question q2 = g.questionBuilder()
            .id("2")
            .subQuestion(b -> b.id("2.1").origin(qOrigin1)
                .answer(a -> a.textValue(SAMPLE_VALUE_1))
            )
            .subQuestion(b -> b.id("2.2").origin(qOrigin2)
                .answer(a -> a.textValue(SAMPLE_VALUE_2))
            )
            .subQuestion(b -> b.id("2.2"))
            .build();

        Map<String, Set<Question>> eqClasses = formEquivalence.compute(
            Stream.of(q1, q2).collect(Collectors.toList())
        );

        assertEquals(1, eqClasses.size());
    }


    private List<Question> createRandomForms(int numberOfForms) {
        return IntStream.range(0, numberOfForms)
            .mapToObj(i -> g.createForm(4))
            .collect(Collectors.toList());
    }

}
