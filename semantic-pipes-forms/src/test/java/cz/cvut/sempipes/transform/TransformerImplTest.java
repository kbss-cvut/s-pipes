package cz.cvut.sempipes.transform;

import cz.cvut.sforms.VocabularyJena;
import cz.cvut.sempipes.constants.SML;
import cz.cvut.sempipes.model.form.Question;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileUtils;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Test;

import java.io.InputStream;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TransformerImplTest {


    @Test
    public void script2FormCreatesSubQuestions() {
        assertEquals(4, generateRootQuestion().getSubQuestions().size());
    }

    @Test
    public void script2FormCreatesSetsRootQuestionLabel() {
        assertEquals("Module of type Bind with constant", generateRootQuestion().getLabel());
    }

    @Test
    public void script2FormCreatesIdSubQuestion() {
        assertTrue(generateRootQuestion().getSubQuestions().stream()
                .anyMatch(s -> RDFS.Resource.getURI().equals(s.getOrigin().toString())));
    }

    @Test
    public void script2FormOrdersQuestionsCorrectly() {
        Question root = generateRootQuestion();
        Question idQuestion = root.getSubQuestions().stream()
                .filter(s -> RDFS.Resource.getURI().equals(s.getOrigin().toString()))
                .collect(Collectors.toList()).get(0);
        Optional<Question> labelQuestion = root.getSubQuestions().stream()
                .filter(s -> s.getOrigin().toString().startsWith(VocabularyJena.s_c_question_origin.toString()))
                .findFirst();

        assertTrue(root.getSubQuestions().stream().
                filter((question -> !idQuestion.equals(question)))
                .allMatch((question -> isPrecedingQuestion(idQuestion, question))));
        assertTrue(!labelQuestion.isPresent() ||
                root.getSubQuestions().stream()
                        .filter(question -> !idQuestion.equals(question))
                        .filter(question -> !labelQuestion.get().equals(question))
                        .allMatch((question -> isPrecedingQuestion(labelQuestion.get(), question))) &&
                        isPrecedingQuestion(idQuestion, labelQuestion.get()));
    }

    private Question generateRootQuestion() {
        InputStream sampleScriptIS = getClass().getResourceAsStream("/hello-world-script.ttl");

        Model sampleScript = ModelFactory.createDefaultModel().read(sampleScriptIS, null, FileUtils.langTurtle);

        Resource module = sampleScript.listSubjects().filterKeep(s ->
                s.getURI() != null && s.getURI().endsWith("bind-person")
        ).next();
        Resource moduleType = sampleScript.getResource(SML.BindWithConstant.toString());

        return new TransformerImpl().script2Form(sampleScript, module, moduleType);
    }

    private boolean isPrecedingQuestion(Question preceding, Question following) {
        if (following.getPrecedingQuestions().contains(preceding)) {
            return true;
        }
        else if (following.getPrecedingQuestions() != null && !following.getPrecedingQuestions().isEmpty()) {
            return following.getPrecedingQuestions().parallelStream()
                    .anyMatch((q) -> isPrecedingQuestion(preceding, q));
        }
        return false;
    }
}