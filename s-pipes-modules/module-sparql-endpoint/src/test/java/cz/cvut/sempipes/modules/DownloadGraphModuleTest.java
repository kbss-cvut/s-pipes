package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import cz.cvut.sempipes.utils.EndpointTestUtils;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDFBase;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DownloadGraphModuleTest {

//    @Ignore // integration test
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

        assertTrue("download location does not exist", Files.exists(Paths.get(URI.create(outputFileUrl))));

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
        assertEquals("Size of Model returned from the DownloadGraphModule",0, returnedDefaultModelSize);
        assertEquals("Size of retrieved and queried model differs : ", queriedTriplesCount, retrievedTriplesCount);
        assertEquals("Size of persisted model and queried model differs : ", queriedTriplesCount, persistedModelSize[0]);
        assertEquals("Size of retrieved  and persisted model differs : ", retrievedTriplesCount, persistedModelSize[0]);

    }
}