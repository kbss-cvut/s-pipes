package cz.cvut.sempipes.repository;

import cz.cvut.sempipes.JenaTestUtils;
import cz.cvut.sempipes.TestConstants;
import cz.cvut.sempipes.manager.OntologyDocumentManager;
import cz.cvut.sempipes.util.JenaUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.hamcrest.CoreMatchers;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.*;

/**
 * Created by Miroslav Blasko on 31.7.16.
 */
@RunWith(MockitoJUnitRunner.class)
public class SMScriptCollectionRepositoryTest {


    @Mock
    OntologyDocumentManager ontoDocManager;// = mock(OntologyDocumentManager.class);

    @InjectMocks
    SMScriptCollectionRepository scriptCollectionRepository;

    final OntModel sampleOntology = getSampleOntology();
    final String sampleOntologyUri = JenaUtils.getBaseUri(getSampleOntology());

    @Test
    public void getModules() throws Exception {

        //given
        given(ontoDocManager.getOntology(sampleOntologyUri)).willReturn(getSampleOntology());

        //when
        List<Resource> modules = scriptCollectionRepository.getModules(Collections.singleton(sampleOntologyUri));

        //then
        assertEquals(modules.size(), 3);
        // TODO better matching by junit5 or assertThat(modules, CoreMatcher.*);
    }

    @Ignore
    @Test
    public void getModuleTypes() throws Exception {

    }

    @Test
    public void getFunctions() throws Exception {

        //given
        given(ontoDocManager.getOntology(sampleOntologyUri)).willReturn(getSampleOntology());

        //when
        List<Resource> functions = scriptCollectionRepository.getFunctions(Collections.singleton(sampleOntologyUri));

        //then
        assertEquals(functions.size(), 2);

    }

    @Test
    public void getResource() throws Exception {

        //given
        given(ontoDocManager.getOntology(sampleOntologyUri)).willReturn(getSampleOntology());

        Resource sampleResource = getSampleOntology().listResourcesWithProperty(RDF.type).next();

        //when
        Resource resource = scriptCollectionRepository.getResource(sampleResource.getURI(), sampleOntologyUri);

        //then
        assertEquals(sampleResource, resource);
    }

    @Ignore
    @Test
    public void getAlternativeEntityIds() throws Exception {        // get all baseIRIs

    }

    private OntModel getSampleOntology() {
        return JenaTestUtils.loadOntologyClosureFromResources("/sample/sample.ttl");
    }

}