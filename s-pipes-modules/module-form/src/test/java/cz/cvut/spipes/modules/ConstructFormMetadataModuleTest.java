package cz.cvut.spipes.modules;

import cz.cvut.sforms.SformsVocabularyJena;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.test.JenaTestUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.vocabulary.RDF;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConstructFormMetadataModuleTest {

    @Test
    void executeSelf() throws Exception {
        ConstructFormMetadataModule module = new ConstructFormMetadataModule();

        Model inputModel = JenaTestUtils.laodModelFromResource("/sample-form.ttl");

        ExecutionContext inputEC = ExecutionContextFactory.createContext(inputModel);

        module.setInputContext(inputEC);

        ExecutionContext outputEC = module.executeSelf();

        Model outputModel = outputEC.getDefaultModel();

        assertEquals(
            getNumberOfFormEntities(inputModel),
            getNumberOfStatementForProperty(outputModel, SformsVocabularyJena.s_p_has_origin_path)
        );

        assertEquals(
            getNumberOfFormEntities(inputModel),
            getNumberOfStatementForProperty(outputModel, SformsVocabularyJena.s_p_has_origin_path_id)
        );
    }

    private int getNumberOfQuestions(Model formModel) {
        return formModel.listSubjects()
            .filterKeep(
                subj -> subj.hasProperty(RDF.type, SformsVocabularyJena.s_c_question)
            ).toList().size();
    }

    private int getNumberOfAnswers(Model formModel) {
        return formModel.listSubjects()
            .filterKeep(
                subj -> subj.hasProperty(RDF.type, SformsVocabularyJena.s_c_answer)
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