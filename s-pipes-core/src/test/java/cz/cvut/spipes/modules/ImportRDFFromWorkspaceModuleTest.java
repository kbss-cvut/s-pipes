package cz.cvut.spipes.modules;

import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.environment.generator.OntologyGenerator;
import cz.cvut.spipes.exception.ContextNotFoundException;
import cz.cvut.spipes.manager.OntoDocManager;
import org.apache.jena.rdf.model.Model;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.BDDMockito.given;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ImportRDFFromWorkspaceModuleTest {

    @Mock
    OntoDocManager ontoDocManager;

    @Test
    @Disabled
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


    @Test
    @Disabled
    public void executeSelfWithInvalidBaseUriThrowsException() throws Exception {

        Model sampleModel = OntologyGenerator.getSampleModel();

        ImportRDFFromWorkspaceModule module = new ImportRDFFromWorkspaceModule();
        module.setOntologyDocumentManager(ontoDocManager);
        module.setBaseUri(OntologyGenerator.getSampleOntologyUri());
        module.setIgnoreImports(true);

        assertThrows(ContextNotFoundException.class,
            () -> {
                //given
                given(ontoDocManager.getModel(OntologyGenerator.getSampleOntologyUri()))
                    .willReturn(null);

                //when
                ExecutionContext ec = module.executeSelf();

                //then
                verify(ontoDocManager, times(1)).getModel(OntologyGenerator.getSampleOntologyUri());
            });
    }

}