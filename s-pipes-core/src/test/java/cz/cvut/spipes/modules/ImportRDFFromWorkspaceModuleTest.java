package cz.cvut.spipes.modules;

import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.environment.generator.OntologyGenerator;
import cz.cvut.spipes.exception.ContextNotFoundException;
import cz.cvut.spipes.manager.OntoDocManager;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import org.apache.jena.rdf.model.Model;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.BDDMockito.given;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Created by Miroslav Blasko on 20.2.17.
 */
@RunWith(MockitoJUnitRunner.class)
public class ImportRDFFromWorkspaceModuleTest {

    @Mock
    OntoDocManager ontoDocManager;

    @Test
    @Ignore
    public void executeSelfWithBaseUriAndIgnoredImports() throws Exception {

        Model sampleModel = OntologyGenerator.getSampleModel();

        ImportRDFFromWorkspaceModule module = new ImportRDFFromWorkspaceModule();
        module.setOntologyDocumentManager(ontoDocManager);
        module.setBaseUri(OntologyGenerator.getSampleOntologyUri());
        module.setIgnoreImports(true);

        //given
        given(ontoDocManager.getModel(OntologyGenerator.getSampleOntologyUri()))
                .willReturn(sampleModel);

        //when
        ExecutionContext ec = module.executeSelf();

        //then
        verify(ontoDocManager, times(1)).getModel(OntologyGenerator.getSampleOntologyUri());
        assertEquals(ec.getDefaultModel(), sampleModel);
        assertTrue(ec.getVariablesBinding().isEmpty());
    }


    @Test(expected = ContextNotFoundException.class)
    @Ignore
    public void executeSelfWithInvalidBaseUriThrowsException() throws Exception {

        Model sampleModel = OntologyGenerator.getSampleModel();

        ImportRDFFromWorkspaceModule module = new ImportRDFFromWorkspaceModule();
        module.setOntologyDocumentManager(ontoDocManager);
        module.setBaseUri(OntologyGenerator.getSampleOntologyUri());
        module.setIgnoreImports(true);

        //given
        given(ontoDocManager.getModel(OntologyGenerator.getSampleOntologyUri()))
                .willReturn(null);

        //when
        ExecutionContext ec = module.executeSelf();

        //then
        verify(ontoDocManager, times(1)).getModel(OntologyGenerator.getSampleOntologyUri());
    }

}