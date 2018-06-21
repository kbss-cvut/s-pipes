package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.KBSS_MODULE;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import cz.cvut.sempipes.engine.VariablesBinding;
import cz.cvut.sempipes.impl.GraphChunkedDownload;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadGraphModule extends AnnotatedAbstractModule {

    private static final String TYPE_URI = KBSS_MODULE.uri + "sparql-endpoint-download-graph";
    private static final String TYPE_PREFIX = TYPE_URI + "/";
    private static final int DEFAULT_PAGE_SIZE = 10000;
    private static final Logger LOG = LoggerFactory.getLogger(DownloadGraphModule.class);

    @Parameter(urlPrefix = TYPE_PREFIX, name = "named-graph-id")
    private String namedGrapheId;

    @Parameter(urlPrefix = TYPE_PREFIX, name = "endpoint-url")
    private String endpointUrl;

    @Parameter(urlPrefix = TYPE_PREFIX, name = "output-resource-variable")
    private String outputResourceVariable;

    @Parameter(urlPrefix = TYPE_PREFIX, name = "page-size")
    private Integer pageSize = DEFAULT_PAGE_SIZE;

    public String getNamedGrapheId() {
        return namedGrapheId;
    }

    public void setNamedGrapheId(String namedGrapheId) {
        this.namedGrapheId = namedGrapheId;
    }

    public String getTypeURI() {
        return TYPE_URI;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getOutputResourceVariable() {
        return outputResourceVariable;
    }

    public void setOutputResourceVariable(String outputResourceVariable) {
        this.outputResourceVariable = outputResourceVariable;
    }

    @Override
    ExecutionContext executeSelf() {

        Path file = createTempFile();

        try (OutputStream os = new FileOutputStream(file.toString())) {

            GraphChunkedDownload downlaoder = new GraphChunkedDownload(namedGrapheId, endpointUrl, pageSize) {
                @Override
                protected void processPartialModel(Model partialModel) {
                    RDFDataMgr.write(os, partialModel, Lang.N3);
                }
            };
            downlaoder.execute();

            VariablesBinding vb = new VariablesBinding(outputResourceVariable,
                ResourceFactory.createPlainLiteral(file.toUri().toURL().toString())
            );

            return ExecutionContextFactory.createContext(
                this.executionContext.getDefaultModel(),
                vb
            );
        } catch (IOException e) {
            throw new RuntimeException("Could not save data into temporary file " + file + ".", e);
        }
    }

    private Path createTempFile() {
        Path file;
        try {
            file = Files.createTempFile("downloaded-graph-", ".nt");
        } catch (IOException e) {
            throw new RuntimeException("Could not create temporary file.", e);
        }
        return file;
    }

}
