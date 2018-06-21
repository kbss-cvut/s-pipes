package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.engine.ExecutionContextFactory;
import cz.cvut.sempipes.utils.EndpointTestUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Test;

public class DownloadGraphModuleTest {

    @Ignore
    @Test
    public void executeSelfDownloadsFileInNtFormat() throws Exception {
        DownloadGraphModule module = new DownloadGraphModule();

        String endpointUrl = "https://linked.opendata.cz/sparql";
        String namedGraphId = "http://linked.opendata.cz/resource/dataset/vavai/programmes";

        module.setInputContext(ExecutionContextFactory.createEmptyContext());
        module.setEndpointUrl(endpointUrl);
        module.setNamedGrapheId(namedGraphId);
        module.setPageSize(100000);
        module.setOutputResourceVariable("outputFileUrl");
        String outputFileUrl = module.executeSelf().getVariablesBinding().getNode("outputFileUrl").toString();

        Model model = ModelFactory.createDefaultModel().read(outputFileUrl);

        long queriedTriplesCount = EndpointTestUtils.getNumberOfTriples(endpointUrl, namedGraphId);
        long retrievedTriplesCount = model.size();

        System.out.println("Output model size : " + model.size());
        assertEquals("Size of retrieved and queried model differs : ", queriedTriplesCount, retrievedTriplesCount);
    }
}