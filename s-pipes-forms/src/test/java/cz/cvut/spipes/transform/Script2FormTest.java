package cz.cvut.spipes.transform;

import cz.cvut.spipes.constants.SML;
import cz.cvut.sforms.FormUtils;
import cz.cvut.sforms.model.Question;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileUtils;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class Script2FormTest {


    @Test(expected = IllegalArgumentException.class)
    public void script2FormWithLocalModuleUriThrowsException()  {
        Model sampleScript = getScript("/hello-world-script.ttl");

        Resource bindWithConstant = sampleScript.getResource("http://topbraid.org/sparqlmotionlib#BindWithConstant");

        Resource bindFirstName = sampleScript.getResource("null");

       new TransformerImpl().script2Form(sampleScript, bindFirstName, bindWithConstant);
    }


    @Test(expected = IllegalArgumentException.class)
    public void script2FormWithLocalModuleTypeUriThrowsException()  {
        Model sampleScript = getScript("/hello-world-script.ttl");

        Resource bindWithConstant = sampleScript.getResource("null");

        Resource bindFirstName = sampleScript.getResource("http://fel.cvut.cz/ontologies/s-pipes-editor/sample-script/bind-first-name");

        new TransformerImpl().script2Form(sampleScript, bindFirstName, bindWithConstant);
    }


    @Ignore
    @Test
    public void script2FormWithNoDataCreatesIRIQuestion() {
        Model sampleScript = getScript("/hello-world-script.ttl");

        Resource bindWithConstant = sampleScript.getResource("http://topbraid.org/sparqlmotionlib#BindWithConstant");

        Resource bindFirstName = sampleScript.getResource("http://fel.cvut.cz/ontologies/s-pipes-editor/sample-script/bind-first-name");

        Question rootQ = new TransformerImpl().script2Form(sampleScript, bindFirstName, bindWithConstant);

        Question iriQuestion = FormUtils.flatten(rootQ).stream().
                filter(q -> RDFS.Resource.getURI().equals(q.getOrigin().toString()))
                .findAny().orElse(null);

        assertNotNull(iriQuestion);

        assertTrue(iriQuestion.getAnswers().stream()
                .anyMatch(a -> bindFirstName.toString().equals(a.getCodeValue().toString())));
    }

    @Ignore
    @Test
    public void script2FormWithNoDataCreatesLabelQuestion() {
        Model sampleScript = getScript("/hello-world-script.ttl");

        Resource bindWithConstant = sampleScript.getResource("http://topbraid.org/sparqlmotionlib#BindWithConstant");

        Resource bindFirstName = sampleScript.getResource("http://fel.cvut.cz/ontologies/s-pipes-editor/sample-script/bind-first-name");

        Question rootQ = new TransformerImpl().script2Form(sampleScript, bindFirstName, bindWithConstant);

        assertTrue(FormUtils.flatten(rootQ).stream().
                anyMatch(q -> RDFS.label.getURI().equals(q.getOrigin().toString())));
    }

    @Ignore
    @Test
    public void script2FormWithNoDataCreatesFormForType() {
        Model sampleScript = getScript("/hello-world-script.ttl");

        Resource bindWithConstant = sampleScript.getResource("http://topbraid.org/sparqlmotionlib#BindWithConstant");

        Resource bindFirstName = sampleScript.getResource("http://fel.cvut.cz/ontologies/s-pipes-editor/sample-script/bind-first-name");

        Question rootQ = new TransformerImpl().script2Form(sampleScript, bindFirstName, bindWithConstant);

        assertEquals(bindFirstName.toString(), rootQ.getOrigin().toString());

        List<Question> leafQuestions = FormUtils.flatten(rootQ).stream()
            .filter(q -> q.getSubQuestions().isEmpty())
            .collect(Collectors.toList());

        assertEquals( 4, leafQuestions.size());
    }


    @Ignore
    @Test
    public void script2FormCreatesSubQuestions() {
        assertEquals(1, generateRootQuestion().getSubQuestions().size());
        assertEquals(4, generateRootQuestion().getSubQuestions().stream()
                .flatMap((q) -> q.getSubQuestions().stream())
                .collect(Collectors.toList())
                .size());
    }

    @Ignore
    @Test
    public void script2FormCreatesCorrectLayoutClasses() {
        assertTrue(generateRootQuestion().getLayoutClass().contains("form"));
        assertTrue(generateRootQuestion().getSubQuestions().stream()
                .allMatch((s) -> s.getLayoutClass().containsAll(new HashSet<String>() {{
                    add("section");
                    add("wizard-step");
                }})));
    }

    @Ignore
    @Test
    public void script2FormCreatesCorrectLabels() {
        assertEquals("Module of type Bind with constant", generateRootQuestion().getLabel());
        assertTrue(generateRootQuestion().getSubQuestions().stream()
                .allMatch((q) -> q.getLabel().equals("Module configuration")));
    }

    @Ignore
    @Test
    public void script2FormCreatesIdSubQuestion() {
        assertTrue(generateRootQuestion().getSubQuestions().stream()
                .flatMap((q) -> q.getSubQuestions().stream())
                .anyMatch(s -> RDFS.Resource.getURI().equals(s.getOrigin().toString())));
    }

    @Ignore
    @Test
    public void script2FormOrdersQuestionsCorrectly() {
        Question wizardStep = new LinkedList<>(generateRootQuestion().getSubQuestions()).get(0);
        Question idQuestion = wizardStep.getSubQuestions().stream()
                .filter(s -> RDFS.Resource.getURI().equals(s.getOrigin().toString()))
                .collect(Collectors.toList()).get(0);

        Optional<Question> labelQuestion = wizardStep.getSubQuestions().stream()
                .filter(s -> s.getOrigin().toString().equals(RDFS.label.toString()))
                .findFirst();

        assertTrue(wizardStep.getSubQuestions().stream().
                filter((question -> !idQuestion.equals(question)))
                .allMatch((question -> isPrecedingQuestion(idQuestion, question))));
        assertTrue(!labelQuestion.isPresent() ||
                wizardStep.getSubQuestions().stream()
                        .filter(question -> !idQuestion.equals(question))
                        .filter(question -> !labelQuestion.get().equals(question))
                        .allMatch((question -> isPrecedingQuestion(labelQuestion.get(), question))) &&
                        isPrecedingQuestion(idQuestion, labelQuestion.get()));
    }

    @Ignore
    @Test
    public void script2FormAllQuestionsHaveAnswer() {
        Question root = generateRootQuestion();
        assertTrue(root.getSubQuestions().stream().flatMap((q) -> q.getSubQuestions().stream()).allMatch((q) -> q.getAnswers() != null && !q.getAnswers().isEmpty()));
    }

    private Question generateRootQuestion() {
        Model sampleScript = getScript("/hello-world-script.ttl");

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

    private Model getScript(String resourcePath) {
        InputStream sampleScriptIS = getClass().getResourceAsStream(resourcePath);

        return ModelFactory.createDefaultModel().read(sampleScriptIS, "http://myexample/", FileUtils.langTurtle);
    }

}
