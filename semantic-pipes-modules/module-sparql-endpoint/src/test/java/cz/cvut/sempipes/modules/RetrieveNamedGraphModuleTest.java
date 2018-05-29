package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.utils.EndpointTestUtils;
import org.apache.jena.rdf.model.Model;
import static org.junit.Assert.assertEquals;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by Miroslav Blasko on 17.7.17.
 */
public class RetrieveNamedGraphModuleTest {


    @Ignore
    @Test
    public void executeSelf() throws Exception {
        RetrieveNamedGraphModule module = new RetrieveNamedGraphModule();

        String endpointUrl = "https://linked.opendata.cz/sparql";
        String namedGraphUri = "http://linked.opendata.cz/resource/dataset/mfcr/rozvaha";

        module.setEndpointUrl(endpointUrl);
        module.setNamedGrapheUri(namedGraphUri);
        module.setPageSize(100000);
        Model model = module.executeSelf().getDefaultModel();
        long queriedTriplesCount = EndpointTestUtils.getNumberOfTriples(endpointUrl, namedGraphUri);
        long retrievedTriplesCount = model.size();

        System.out.println("Output model size : " + model.size());
        assertEquals("Size of retrieved and queried model differs : ", queriedTriplesCount, retrievedTriplesCount);
    }


//        module.setEndpointUrl("http://onto.fel.cvut.cz/rdf4j-sesame/repositories/europeana.eu");
//        module.setNamedGrapheUri("http://data.europeana.eu/");
//
//    String endpointUrl = "http://onto.fel.cvut.cz/rdf4j-server/repositories/freeclassowl_v1";
//    String namedGraphUri = "http://www.freeclass.eu/freeclass_v1.owl";

}