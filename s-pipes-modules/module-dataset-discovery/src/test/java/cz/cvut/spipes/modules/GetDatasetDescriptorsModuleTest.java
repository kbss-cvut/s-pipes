package cz.cvut.spipes.modules;

import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import org.apache.jena.rdf.model.Model;
import static org.junit.Assert.assertTrue;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by Miroslav Blasko on 5.1.17.
 */
public class GetDatasetDescriptorsModuleTest {

    @Test
    @Ignore
    public void executeSelf() throws Exception {

        //TODO it should work with empty context as well
        ExecutionContext inCtx = ExecutionContextFactory.createEmptyContext();
//        ExecutionContext inCtx = ExecutionContextFactory.createContext(new VariablesBinding(
//                "p-dataset-iri",
//                ResourceFactory.createPlainLiteral("http://linked.opendata.cz/resource/dataset/vavai/tenders/vocabulary")
//        ));

        GetDatasetDescriptorsModule m = new GetDatasetDescriptorsModule();
        m.setPrpDatasetIri("http://linked.opendata.cz/resource/dataset/vavai/tenders/vocabulary");

        m.setInputContext(inCtx);

        ExecutionContext outCtx = m.executeSelf();

        Model outModel = outCtx.getDefaultModel();

        assertTrue(outModel.size() > 0);

    }

}