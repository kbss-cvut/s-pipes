package cz.cvut.spipes.modules;

import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.utils.EndpointTestUtils;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDFBase;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DownloadGraphModuleTest {

    @Disabled // requires online sparql endpoint
    @Test
    public void executeSelfDownloadsFileInNtFormat() throws Exception {
        DownloadGraphModule module = new DownloadGraphModule();

        String endpointUrl = "https://linked.opendata.cz/sparql";
        String namedGraphId = "http://linked.opendata.cz/resource/dataset/gregorian-day-labels";

        module.setInputContext(ExecutionContextFactory.createEmptyContext());
        module.setEndpointUrl(endpointUrl);
        module.setNamedGraphId(namedGraphId);
        module.setPageSize(10);
        module.setOutputResourceVariable("outputFileUrl");
        ExecutionContext ec = module.executeSelf();
        String outputFileUrl = ec.getVariablesBinding().getNode("outputFileUrl").toString();

        assertTrue(Files.exists(Paths.get(URI.create(outputFileUrl))), "download location does not exist");

        System.out.println("file path : " + outputFileUrl);
        Model defaultModel = ec.getDefaultModel();
//        Model model = ModelFactory.createDefaultModel().read(outputFileUrl);

        long[] persistedModelSize = {0};
        RDFDataMgr.parse(new StreamRDFBase(){
            @Override
            public void triple(Triple triple) {
                persistedModelSize[0] ++;
            }
        }, outputFileUrl);

        long queriedTriplesCount = EndpointTestUtils.getNumberOfTriples(endpointUrl, namedGraphId);
        long returnedDefaultModelSize = defaultModel.size();
        long retrievedTriplesCount = module.getNumberOfDownloadedTriples();

        System.out.println("Output default model size : " + defaultModel.size() + ", #retrieved triples :" + retrievedTriplesCount +
                ", #persisted triples : " + persistedModelSize[0] + ",  queriedTriplesCount : " + queriedTriplesCount);
        assertEquals(0, returnedDefaultModelSize, "Size of Model returned from the DownloadGraphModule");
        assertEquals(queriedTriplesCount, retrievedTriplesCount, "Size of retrieved and queried model differs : ");
        assertEquals(queriedTriplesCount, persistedModelSize[0], "Size of persisted model and queried model differs : ");
        assertEquals(retrievedTriplesCount, persistedModelSize[0], "Size of retrieved  and persisted model differs : ");

    }
}