package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import cz.cvut.sempipes.impl.GraphChunkedDownload;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DownloadGraph extends AbstractGraphChunckedDownload {

    private static final Logger LOG = LoggerFactory.getLogger(DownloadGraph.class);

    @Parameter(urlPrefix = TYPE_PREFIX, name = "http://topbraid.org/sparqlmotion#outputVariable")
    private String outputVariable;

// TODO set outputVariable
//        outputVariable = ???
    
    @Override
    ExecutionContext executeSelf() {
        
        try(OutputStream os = new FileOutputStream(outputVariable)){
            GraphChunkedDownload downlaoder = new GraphChunkedDownload(getNamedGrapheId(), getEndpointUrl(), getPageSize()) {
                @Override
                protected void processPartialModel(Model partialModel) {
                    RDFDataMgr.write(os, partialModel, Lang.N3);
                }
            };
            downlaoder.execute();
        }catch(IOException ex){
            throw new RuntimeException(ex);
        }
        
        return ExecutionContextFactory.createContext(this.executionContext.getDefaultModel());
    }
    
}
