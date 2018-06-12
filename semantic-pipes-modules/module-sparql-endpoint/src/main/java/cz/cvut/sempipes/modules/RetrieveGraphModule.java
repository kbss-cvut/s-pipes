package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import cz.cvut.sempipes.impl.GraphChunkedDownload;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetrieveGraphModule extends AbstractGraphChunckedDownload {

    private static final Logger LOG = LoggerFactory.getLogger(RetrieveGraphModule.class);

    @Override
    ExecutionContext executeSelf() {
        Model outputModel = ModelFactory.createDefaultModel();
        GraphChunkedDownload downlaoder = new GraphChunkedDownload(getNamedGrapheId(), getEndpointUrl(), getPageSize()) {
            @Override
            protected void processPartialModel(Model partialModel) {
                outputModel.add(partialModel);
            }
        };
        downlaoder.execute();
        return ExecutionContextFactory.createContext(outputModel);
    }
}
