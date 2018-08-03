package cz.cvut.sforms.test;

import cz.cvut.sforms.model.Answer;
import cz.cvut.sforms.model.Question;
import java.util.List;
import java.util.Optional;
import lombok.Builder;
import lombok.Singular;

public class FormGenerator {

    IdGenerator idGenerator = new SequenceIdGenerator();
    boolean includeAnswer = true;

    public FormGenerator() {
    }

    public FormGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public FormGenerator(FormGenerator copy) {
        this.idGenerator = copy.idGenerator;
    }

    public interface BuilderChain {
        QuestionBuilder next(QuestionBuilder builder);
    }

    @Builder(builderMethodName = "questionBuilder")
    public Question createQuestion(String id,
                                   Boolean includeAnswer,
                                   @Singular("subQuestion") List<BuilderChain> subQuestion) {

        String qId = Optional.ofNullable(id).orElse(idGenerator.nextString());
        this.includeAnswer = Optional.ofNullable(includeAnswer).orElse(this.includeAnswer);
        Question rootQ = createQuestion(qId);


        subQuestion.stream().forEach(
            qb -> {
                Question q = qb.next(new FormGenerator(this).questionBuilder()).build();
                rootQ.getSubQuestions().add(q);
            }
        );

        return rootQ;
    }

    public Question createQuestion(String id) {
        Question q = new Question();
        q.setLabel(createUniqueString("label-q", id));
        if (includeAnswer) {
            q.getAnswers().add(createAnswer(id));
        }
        return q;
    }

    public Answer createAnswer(String id) {
        Answer a = new Answer();
        a.setTextValue(createUniqueString("value", id));
        return a;
    }

    private static String createUniqueString(String prefix, String id) {
        return prefix.concat("-").concat(id);
    }

}
