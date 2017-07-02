package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.VocabularyJena;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import static org.junit.Assert.*;
import org.junit.Test;
import util.JenaTestUtils;

/**
 * Created by Miroslav Blasko on 26.6.17.
 */
public class ConstructFormMetadataModuleTest {

    @Test
    public void executeSelf() throws Exception {
        ConstructFormMetadataModule module = new ConstructFormMetadataModule();

        Model inputModel = JenaTestUtils.laodModelFromResource("/sample-form.ttl");

        ExecutionContext inputEC = ExecutionContextFactory.createContext(inputModel);

        module.setInputContext(inputEC);

        ExecutionContext outputEC = module.executeSelf();

        Model outputModel = outputEC.getDefaultModel();

        assertEquals(
            getNumberOfFormEntities(inputModel),
            getNumberOfStatementForProperty(outputModel, VocabularyJena.s_p_has_origin_path)
        );

        assertEquals(
            getNumberOfFormEntities(inputModel),
            getNumberOfStatementForProperty(outputModel, VocabularyJena.s_p_has_origin_path_id)
        );

    }

    private int getNumberOfQuestions(Model formModel) {
        return formModel.listSubjects()
            .filterKeep(
                subj -> subj.hasProperty(RDF.type, VocabularyJena.s_c_question)
            ).toList().size();
    }

    private int getNumberOfAnswers(Model formModel) {
        return formModel.listSubjects()
            .filterKeep(
                subj -> subj.hasProperty(RDF.type, VocabularyJena.s_c_answer)
            ).toList().size();
    }

    private int getNumberOfFormEntities(Model formModel) {
        return getNumberOfQuestions(formModel) + getNumberOfAnswers(formModel);
    }

    private int getNumberOfStatementForProperty(Model model, Property property) {
        return model.listSubjects()
            .filterKeep(
                subj -> subj.hasProperty(property)
            ).toList().size();
    }

}