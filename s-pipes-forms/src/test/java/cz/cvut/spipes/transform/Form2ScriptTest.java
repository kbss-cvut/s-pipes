package cz.cvut.spipes.transform;

import cz.cvut.sforms.model.Answer;
import cz.cvut.sforms.model.Question;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileUtils;
import org.apache.jena.vocabulary.RDFS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class Form2ScriptTest {

    private Transformer t = new TransformerImpl();
    private InputStream sampleScriptIS = getClass().getResourceAsStream("/hello-world-script.ttl");
    private Model sampleScript = ModelFactory.createOntologyModel().read(sampleScriptIS, null, FileUtils.langTurtle);
    private Question form;

    @BeforeEach
    public void initForm() {
        form = getForm();
    }

    @Disabled
    @Test
    public void basicTransformation() {
        /*Model m = t.form2Script(sampleScript, form, "http://topbraid.org/sparqlmotionlib#BindWithConstant");
        assertTrue(m.listSubjects().toList().stream().anyMatch((s) -> Objects.equals("http://fel.cvut.cz/ontologies/s-pipes-editor/sample-script/bind-person", s.getURI())));
        List<Statement> ps = m.listStatements().toList();
        assertTrue(ps.stream().anyMatch((s) -> Objects.equals(RDFS.label, s.getPredicate())));
        assertTrue(ps.stream().anyMatch((s) -> Objects.equals(
                "http://topbraid.org/sparqlmotionlib#value",
                s.getPredicate().getURI())));
        assertTrue(ps.stream().anyMatch((s) -> Objects.equals(
                "http://topbraid.org/sparqlmotion#outputVariable",
                s.getPredicate().getURI())));
        List<RDFNode> os = m.listObjects().toList();
        assertTrue(os.stream().anyMatch((o) -> o.isLiteral() && Objects.equals("Bind person", o.asLiteral().getString())));
        assertTrue(os.stream().anyMatch((o) -> o.isLiteral() && Objects.equals("person", o.asLiteral().getString())));
        assertTrue(os.stream().anyMatch((o) -> o.isLiteral() && Objects.equals("Robert Plant", o.asLiteral().getString())));*/
    }

    @Disabled
    @Test
    public void regularStatementUpdate() {
        Optional<Question> labelQ = form.getSubQuestions().stream()
                .flatMap((q) -> q.getSubQuestions().stream())
                .filter((q) -> q.getAnswers().stream()
                        .anyMatch((a) -> "Bind person".equals(a.getTextValue())))
                .findFirst();

        assertTrue(labelQ.isPresent());

        labelQ.get().getAnswers().forEach((a) -> a.setTextValue("NEW Bind person"));

        Collection<Model> outputs = t.form2Script(sampleScript, form, "http://topbraid.org/sparqlmotionlib#BindWithConstant");


        assertEquals("NEW Bind person", new LinkedList<>(outputs).get(0)
                .getResource("http://fel.cvut.cz/ontologies/s-pipes-editor/sample-script/bind-person")
                .getProperty(RDFS.label)
                .getString());
    }

    @Test
    public void moduleURIUpdate() {
        Resource bindPerson = sampleScript.getResource("http://fel.cvut.cz/ontologies/s-pipes-editor/sample-script/bind-person");
        int initialSize = bindPerson.listProperties().toList().size();

        assertEquals("Bind person", bindPerson.getProperty(RDFS.label).getString());

        Optional<Question> uriQ = form.getSubQuestions().stream()
                .flatMap((q) -> q.getSubQuestions().stream())
                .filter((q) -> q.getAnswers().stream()
                        .anyMatch((a) -> a.getCodeValue() != null && "http://fel.cvut.cz/ontologies/s-pipes-editor/sample-script/bind-person".equals(a.getCodeValue().toString())))
                .findFirst();

        assertTrue(uriQ.isPresent());

        uriQ.get().getAnswers().forEach((a) -> a.setCodeValue(URI.create("http://fel.cvut.cz/ontologies/s-pipes-editor/sample-script/new-bind-person")));

//        Model outputScript = t.form2Script(sampleScript, form, "http://topbraid.org/sparqlmotionlib#BindWithConstant");
//        Resource newBindPerson = outputScript.getResource("http://fel.cvut.cz/ontologies/s-pipes-editor/sample-script/new-bind-person");

        /*assertTrue(outputScript.getResource(bindPerson.getURI()).listProperties().toList().isEmpty());
        assertEquals(initialSize, newBindPerson.listProperties().toList().size());*/
    }


    private Question getForm() {
        Question uriQ = new Question();
        uriQ.setUri(URI.create("http://onto.fel.cvut.cz/ontologies/documentation/question-4b0e7cf3-efa2-480e-83a5-d262a3b5fcb6"));
        uriQ.setLabel("URI");
        uriQ.setOrigin(URI.create("http://www.w3.org/2000/01/rdf-schema#Resource"));
        Answer uriAnswer = new Answer();
        uriAnswer.setCodeValue(URI.create("http://fel.cvut.cz/ontologies/s-pipes-editor/sample-script/bind-person"));
        uriQ.setAnswers(Collections.singleton(uriAnswer));

        Question labelQ = new Question();
        labelQ.setUri(URI.create("http://onto.fel.cvut.cz/ontologies/documentation/question-74219998-8fbd-401b-ab3a-806938a4103c1"));
        labelQ.setLabel("http://www.w3.org/2000/01/rdf-schema#label");
        labelQ.setOrigin(URI.create("http://www.w3.org/2000/01/rdf-schema#label"));
        labelQ.setPrecedingQuestions(Collections.singleton(uriQ));
        Answer labelAnswer = new Answer();
        labelAnswer.setOrigin(URI.create("http://onto.fel.cvut.cz/ontologies/form/answer-origin/16bfdac593dfed73aa8952f0b494ce58"));
        labelAnswer.setTextValue("Bind person");
        labelQ.setAnswers(Collections.singleton(labelAnswer));

        Question valueQ = new Question();
        valueQ.setUri(URI.create("http://onto.fel.cvut.cz/ontologies/documentation/question-74219998-8fbd-401b-ab3a-806938a4103c2"));
        valueQ.setLabel("http://topbraid.org/sparqlmotionlib#value");
        valueQ.setDescription("value");
        valueQ.setOrigin(URI.create("http://topbraid.org/sparqlmotionlib#value"));
        valueQ.setPrecedingQuestions(Collections.singleton(labelQ));
        Answer valueAnswer = new Answer();
        valueAnswer.setOrigin(URI.create("http://onto.fel.cvut.cz/ontologies/form/answer-origin/7b132fcdebc58b869772def95a5a0aae"));
        valueAnswer.setTextValue("Robert Plant");
        valueQ.setAnswers(Collections.singleton(valueAnswer));

        Question outputVariableQ = new Question();
        outputVariableQ.setUri(URI.create("http://onto.fel.cvut.cz/ontologies/documentation/question-74219998-8fbd-401b-ab3a-806938a4103c3"));
        outputVariableQ.setLabel("http://topbraid.org/sparqlmotion#outputVariable");
        outputVariableQ.setDescription("outputVariable");
        outputVariableQ.setOrigin(URI.create("http://topbraid.org/sparqlmotion#outputVariable"));
        outputVariableQ.setPrecedingQuestions(Collections.singleton(labelQ));
        Answer outputVariableAnswer = new Answer();
        outputVariableAnswer.setOrigin(URI.create("http://onto.fel.cvut.cz/ontologies/form/answer-origin/8b0a44048f58988b486bdd0d245b22a8"));
        outputVariableAnswer.setTextValue("person");
        outputVariableQ.setAnswers(Collections.singleton(outputVariableAnswer));

        Question wizardStep = new Question();
        wizardStep.setUri(URI.create("http://onto.fel.cvut.cz/ontologies/documentation/question-908b5c6e-d871-4d5a-904a-e8a1eab1dc69"));
        wizardStep.setLabel("Module configuration");
        wizardStep.setOrigin(URI.create("http://fel.cvut.cz/ontologies/s-pipes-editor/sample-script/bind-person"));
        HashSet<String> wizardStepLayoutClass = new HashSet<>();
        wizardStepLayoutClass.add("wizard-step");
        wizardStepLayoutClass.add("section");
        wizardStep.setLayoutClass(wizardStepLayoutClass);
        HashSet<Question> wizardStepSubquestions = new HashSet<>();
        wizardStepSubquestions.add(uriQ);
        wizardStepSubquestions.add(labelQ);
        wizardStepSubquestions.add(valueQ);
        wizardStepSubquestions.add(outputVariableQ);
        wizardStep.setSubQuestions(wizardStepSubquestions);

        Question form = new Question();
        form.setUri(URI.create("http://onto.fel.cvut.cz/ontologies/documentation/question-d1dca1d0-eb44-4391-9ac8-7ca708ce4404"));
        form.setLabel("Module of type Bind with constant");
        form.setOrigin(URI.create("http://fel.cvut.cz/ontologies/s-pipes-editor/sample-script/bind-person"));
        form.setLayoutClass(Collections.singleton("from"));
        form.setSubQuestions(Collections.singleton(wizardStep));

        return form;
    }
}
