package cz.cvut.spipes.repository;

import cz.cvut.spipes.manager.OntologyDocumentManager;
import cz.cvut.spipes.util.JenaUtils;
import java.util.Collections;
import java.util.List;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.BDDMockito.given;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import cz.cvut.spipes.test.JenaTestUtils;

@ExtendWith(MockitoExtension.class)
public class SMScriptCollectionRepositoryTest {


    @Mock
    OntologyDocumentManager ontoDocManager;// = mock(OntologyDocumentManager.class);

    @InjectMocks
    SMScriptCollectionRepository scriptCollectionRepository;

    final String sampleOntologyUri = JenaUtils.getBaseUri(getSampleOntology());

    @Test
    public void getModules() {

        //given
        given(ontoDocManager.getOntology(sampleOntologyUri)).willReturn(getSampleOntology());

        //when
        List<Resource> modules = scriptCollectionRepository.getModules(Collections.singleton(sampleOntologyUri));

        //then
        assertEquals(3, modules.size());
        // TODO better matching by junit5 or assertThat(modules, CoreMatcher.*);
    }

    @Disabled
    @Test
    public void getModuleTypes() throws Exception {

    }

    @Test
    public void getPipelineFunctions() {

        //given
        given(ontoDocManager.getOntology(sampleOntologyUri)).willReturn(getSampleOntology());

        //when
        List<Resource> functions = scriptCollectionRepository.getPipelineFunctions(Collections.singleton(sampleOntologyUri));

        //then
        assertEquals(2, functions.size());

    }

    @Disabled
    @Test
    public void getResource() {

        //given
        given(ontoDocManager.getOntology(sampleOntologyUri)).willReturn(getSampleOntology());

        Resource sampleResource = getSampleOntology().listResourcesWithProperty(RDF.type).next();

        //when
        Resource resource = scriptCollectionRepository.getResource(sampleResource.getURI(), sampleOntologyUri);

        //then
        assertEquals(sampleResource, resource);
    }

    @Disabled
    @Test
    public void getAlternativeEntityIds() throws Exception {        // get all baseIRIs

    }

    private OntModel getSampleOntology() {
        return JenaTestUtils.loadOntologyClosureFromResources("/sample/sample.ttl");
    }

}