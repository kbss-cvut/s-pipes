package cz.cvut.sempipes.transform;

import cz.cvut.sempipes.constants.SML;
import cz.cvut.sempipes.model.qam.Question;
import java.io.InputStream;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileUtils;
import org.apache.jena.vocabulary.RDFS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

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
        root.getSubQuestions().stream().forEach((q) -> {

        });
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
}