package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.engine.VariablesBinding;
import cz.cvut.spipes.impl.GraphChunkedDownload;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import cz.cvut.spipes.modules.annotations.SPipesModule;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SPipesModule(label = "sparql endpoint download graph", comment = "Downloads named graph namedGraphId from sparql endpoint endpointUrl.")
public class DownloadGraphModule extends AnnotatedAbstractModule {

    private static final String TYPE_URI = KBSS_MODULE.uri + "sparql-endpoint-download-graph";
    private static final String TYPE_PREFIX = TYPE_URI + "/";
    private static final int DEFAULT_PAGE_SIZE = 10000;
    private static final Logger LOG = LoggerFactory.getLogger(DownloadGraphModule.class);

    @Parameter(urlPrefix = TYPE_PREFIX, name = "named-graph-id", comment = "Named graph id")
    private String namedGraphId;

    @Parameter(urlPrefix = TYPE_PREFIX, name = "endpoint-url", comment = "Endpoint url")
    private String endpointUrl;

    @Parameter(urlPrefix = TYPE_PREFIX, name = "output-resource-variable", comment = "Output resource variable")
    private String outputResourceVariable;

    @Parameter(urlPrefix = TYPE_PREFIX, name = "page-size", comment = "Page size. Default value is 10000.")
    private Integer pageSize = DEFAULT_PAGE_SIZE;

    protected long numberOfDownloadedTriples;

    public String getNamedGraphId() {
        return namedGraphId;
    }

    public void setNamedGraphId(String namedGraphId) {
        this.namedGraphId = namedGraphId;
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

    public long getNumberOfDownloadedTriples() {
        return numberOfDownloadedTriples;
    }

    public void setNumberOfDownloadedTriples(long numberOfDownloadedTriples) {
        this.numberOfDownloadedTriples = numberOfDownloadedTriples;
    }

    @Override
    ExecutionContext executeSelf() {

        Path file = createTempFile();

        try (OutputStream os = new FileOutputStream(file.toString())) {

            GraphChunkedDownload downlaoder = new GraphChunkedDownload(endpointUrl, namedGraphId, pageSize) {
                @Override
                protected void processPartialModel(Model partialModel) {
                    numberOfDownloadedTriples += partialModel.size();
                    LOG.trace("persisting partial download, {} triples from (<{}>,<{}>)",
                            partialModel.size(), endpointUrl, namedGraphId);
                    RDFDataMgr.write(os, partialModel, Lang.NTRIPLES);
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
            LOG.trace("persisting downloaded graph (<{}>,<{}>), to file \"{}\"", endpointUrl, namedGraphId, file.toString());
        } catch (IOException e) {
            throw new RuntimeException("Could not create temporary file.", e);
        }
        return file;
    }

}
