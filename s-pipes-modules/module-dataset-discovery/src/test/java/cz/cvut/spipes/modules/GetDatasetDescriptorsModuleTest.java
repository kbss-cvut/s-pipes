package cz.cvut.spipes.modules;

import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import org.apache.jena.rdf.model.Model;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class GetDatasetDescriptorsModuleTest {

    @Test
    @Disabled
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