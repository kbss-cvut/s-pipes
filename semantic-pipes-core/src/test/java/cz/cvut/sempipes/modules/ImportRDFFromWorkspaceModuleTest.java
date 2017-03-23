package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.environment.generator.OntologyGenerator;
import cz.cvut.sempipes.exception.ContextNotFoundException;
import cz.cvut.sempipes.manager.OntoDocManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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