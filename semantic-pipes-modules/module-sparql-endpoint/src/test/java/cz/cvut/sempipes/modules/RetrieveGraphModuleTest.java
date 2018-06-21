package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.engine.ExecutionContextFactory;
import cz.cvut.sempipes.utils.EndpointTestUtils;
import org.apache.jena.rdf.model.Model;
import static org.junit.Assert.assertEquals;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by Miroslav Blasko on 17.7.17.
 */
public class RetrieveGraphModuleTest {


    @Ignore
    @Test
    public void executeSelf() throws Exception {
        RetrieveGraphModule module = new RetrieveGraphModule();

        String endpointUrl = "https://linked.opendata.cz/sparql";
        String namedGraphId = "http://linked.opendata.cz/resource/dataset/vavai/programmes";

        module.setInputContext(ExecutionContextFactory.createEmptyContext());
        module.setEndpointUrl(endpointUrl);
        module.setNamedGrapheId(namedGraphId);
        module.setPageSize(100000);
        Model model = module.executeSelf().getDefaultModel();
        long queriedTriplesCount = EndpointTestUtils.getNumberOfTriples(endpointUrl, namedGraphId);
        long retrievedTriplesCount = model.size();

        System.out.println("Output model size : " + model.size());
        assertEquals("Size of retrieved and queried model differs : ", queriedTriplesCount, retrievedTriplesCount);
    }
}