package cz.cvut.sforms.test;

import cz.cvut.sforms.model.Answer;
import cz.cvut.sforms.model.Question;
import cz.cvut.sforms.util.FormUtils;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Singular;
import org.jetbrains.annotations.NotNull;

public class FormGenerator {

    IdGenerator idGenerator = new SequenceIdGenerator();
    Random r = new Random();

    @Getter @Setter
    private boolean includeAnswer = true;
    @Getter @Setter
    private boolean generateUri = true;

    private static final String URI_PREFIX = "http://example.org/";

    public FormGenerator() {
    }

    public FormGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public FormGenerator(FormGenerator copy) {
        this.idGenerator = copy.idGenerator;
        this.includeAnswer = copy.includeAnswer;
        this.generateUri = copy.generateUri;
    }

    private static String createUniqueString(String prefix, String id) {
        return prefix.concat("-").concat(id);
    }

    @Builder(builderMethodName = "questionBuilder")
    public Question createQuestion(final String id,
                                   final String label,
                                   final URI origin,
                                   final Boolean includeAnswer,
                                   final Boolean generateUri,
                                   final @Singular("answer") List<BuilderChain<AnswerBuilder>> answer,
                                   final @Singular("subQuestion") List<BuilderChain<QuestionBuilder>> subQuestion) {

        String qId = Optional.ofNullable(id).orElse(idGenerator.nextString());

        boolean includeAnswerLocal = Optional.ofNullable(includeAnswer).orElse(this.includeAnswer);
        boolean generateUriLocal = Optional.ofNullable(generateUri).orElse(this.generateUri);

        Question rootQ = createQuestion(
            qId,
            includeAnswerLocal && answer.isEmpty(),
            generateUriLocal
        );

        Optional.ofNullable(label)
            .ifPresent(rootQ::setLabel);

        Optional.ofNullable(origin)
            .ifPresent(rootQ::setOrigin);

        answer.forEach(
            ab -> {
                Answer a = ab.next(new FormGenerator(this).answerBuilder()).build();
                rootQ.getAnswers().add(a);
            }
        );

        subQuestion.forEach(
            qb -> {
                Question q = qb.next(new FormGenerator(this).questionBuilder()).build();
                rootQ.getSubQuestions().add(q);
            }
        );

        return rootQ;
    }


    @Builder(builderMethodName = "answerBuilder")
    public Answer createAnswer(final String id,
                               final String textValue,
                               final URI codeValue,
                               final URI origin,
                               final Boolean generateUri) {

        if ((textValue != null) && (codeValue != null)) {
            throw new IllegalStateException(
                "Answer builder is trying to assign " +
                "text value and code value at the same time."
            );
        }

        String qId = Optional.ofNullable(id).orElse(idGenerator.nextString());
        boolean generateUriLocal = Optional.ofNullable(generateUri).orElse(this.generateUri);

        Answer a = createAnswer(qId, generateUriLocal);

        Optional.ofNullable(textValue)
            .ifPresent(a::setTextValue);

        Optional.ofNullable(codeValue)
            .ifPresent(a::setCodeValue);

        Optional.ofNullable(origin)
            .ifPresent(a::setOrigin);

        return a;
    }


    public Question createForm(int numberOfQuestions) {

        if (numberOfQuestions < 1) {
            throw new IllegalArgumentException(
                String.format(
                    "Number of questions is %d, which is less than 1.",
                    numberOfQuestions
                )
            );
        }

        Question root = createQuestion();

        IntStream.range(1, numberOfQuestions).forEach(
            i -> pickRandomQuestion(root).getSubQuestions().add(createQuestion())
        );
        return root;
    }

    public Question createQuestion(String id) {
       return createQuestion(id, this.includeAnswer, this.generateUri);
    }


    public Answer createAnswer(String id) {
        Answer a = new Answer();
        a.setTextValue(createUniqueString("value", id));
        return a;
    }

    public Answer createAnswer(String id, boolean generateUri) {
        Answer a = new Answer();
        if (generateUri) {
            a.setUri(createAnswerUri(id));
        }
        a.setTextValue(createUniqueString("value", id));
        return a;
    }



    private Question createQuestion(String id, boolean includeAnswer, boolean generateUri) {
        Question q = new Question();
        q.setLabel(createUniqueString("label-q", id));
        if (includeAnswer) {
            q.getAnswers().add(createAnswer(id));
        }
        if (generateUri) {
            q.setUri(createQuestionUri(id));
        }

        return q;
    }

    @NotNull
    private static URI createQuestionUri(String id) {
        return URI.create(URI_PREFIX + "q-" + id);
    }

    @NotNull
    private URI createAnswerUri(String id) {
        return URI.create(URI_PREFIX + "a-" + id);
    }


    private Question createQuestion() {
        return createQuestion(idGenerator.nextString());
    }

    @NotNull
    private Question pickRandomQuestion(@NotNull Question formRoot) {
        List<Question> questions = FormUtils.flatten(Stream.of(formRoot)).collect(Collectors.toList());

        return questions.get(r.nextInt(questions.size()));
    }

    public interface BuilderChain<T> {
        T next(T builder);
    }

}
