package cz.cvut.spipes.modules;


import cz.cvut.sforms.SFormsVocabularyJena;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.test.JenaTestUtils;
import org.apache.jena.rdf.model.Model;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConstructTextualViewModuleTest {

    @Test
    void executeSelfByDefault_attachTextualViewToAllQuestions() {

        ConstructTextualViewModule module = getModuleWithLoadedSampleForm();
        Model outputModel = module.executeSelf().getDefaultModel();

        int outputTexutalViewsCount = outputModel.listSubjectsWithProperty(SFormsVocabularyJena.s_p_textual_view).toList().size();
        assertEquals(6, outputTexutalViewsCount);
    }

    @Test
    void executeSelfWithUnsetProcessNonRootQuestion_attachTextualViewToRootQuestionsOnly() {

        ConstructTextualViewModule module = getModuleWithLoadedSampleForm();
        module.setProcessNonRootQuestions(false);
        Model outputModel = module.executeSelf().getDefaultModel();

        int outputTexutalViewsCount = outputModel.listSubjectsWithProperty(SFormsVocabularyJena.s_p_textual_view).toList().size();
        assertEquals(1, outputTexutalViewsCount);
    }

    private ConstructTextualViewModule getModuleWithLoadedSampleForm() {
        ConstructTextualViewModule module = new ConstructTextualViewModule();
        Model inputModel = JenaTestUtils.laodModelFromResource("/sample-form.ttl");
        ExecutionContext inputEC = ExecutionContextFactory.createContext(inputModel);
        module.setInputContext(inputEC);
        return module;
    }
}
