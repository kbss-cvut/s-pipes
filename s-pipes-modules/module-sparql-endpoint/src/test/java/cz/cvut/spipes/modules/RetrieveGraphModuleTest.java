package cz.cvut.spipes.modules;

import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.utils.EndpointTestUtils;
import org.apache.jena.rdf.model.Model;
import static org.junit.Assert.assertEquals;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by Miroslav Blasko on 17.7.17.
 */
public class RetrieveGraphModuleTest {

//    @Ignore // integration test
    @Test
    public void executeSelf() throws Exception {
        RetrieveGraphModule module = new RetrieveGraphModule();

        String endpointUrl = "https://linked.opendata.cz/sparql";
        String namedGraphId = "http://linked.opendata.cz/resource/dataset/gregorian-day-labels";

        module.setInputContext(ExecutionContextFactory.createEmptyContext());
        module.setEndpointUrl(endpointUrl);
        module.setNamedGraphId(namedGraphId);
        module.setPageSize(10);
        Model model = module.executeSelf().getDefaultModel();
        long queriedTriplesCount = EndpointTestUtils.getNumberOfTriples(endpointUrl, namedGraphId);
        long retrievedTriplesCount = model.size();

        System.out.println("Output model size : " + model.size());
        assertEquals("Size of retrieved and queried model differs : ", queriedTriplesCount, retrievedTriplesCount);
    }
}