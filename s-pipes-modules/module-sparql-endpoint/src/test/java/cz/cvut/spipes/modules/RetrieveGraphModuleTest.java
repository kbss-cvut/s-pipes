package cz.cvut.spipes.modules;

import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.utils.EndpointTestUtils;
import org.apache.jena.rdf.model.Model;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RetrieveGraphModuleTest {

    @Disabled //requires online sparql endpoint
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
        assertEquals(queriedTriplesCount, retrievedTriplesCount, "Size of retrieved and queried model differs : ");
    }
}