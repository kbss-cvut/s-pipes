package cz.cvut.spipes.logging;

import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.engine.ExecutionEngine;
import cz.cvut.spipes.engine.ExecutionEngineFactory;
import static cz.cvut.spipes.logging.AdvancedLoggingProgressListener.*;
import cz.cvut.spipes.modules.TestIdentityModule;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Test;

public class AdvancedLoggingProgressListenerTest {


    @Ignore
    @Test
    public void testSingleRun() {
        singleRun();
    }

    private void singleRun() {
        final TestIdentityModule m = new TestIdentityModule();

        final Model model = ModelFactory.createDefaultModel();
        final Resource r = model.createResource();
        model.add(r, RDFS.label, "Test1");
        m.setConfigurationResource(r);

        final ExecutionEngine e = ExecutionEngineFactory.createEngine();
        final AdvancedLoggingProgressListener l = new AdvancedLoggingProgressListener(getLoggerConfiguration());
        e.addProgressListener(l);
        e.executePipeline(m, ExecutionContextFactory.createEmptyContext());
        e.removeProgressListener(l);
    }

    private Resource getLoggerConfiguration() {
        Model m = ModelFactory.createDefaultModel();
        Resource root = m.createResource();
        m.add(root, P_RDF4J_SERVER_URL, "http://localhost:58080/rdf4j-server");
        m.add(root, P_METADATA_REPOSITORY_NAME, "logging-experiment");
//        m.add(root, P_DATA_REPOSITORY_NAME, "logging-experiment");
        m.add(root, P_PIPELINE_EXECUTION_GROUP_ID, "my-execution-group");
        return root;
    }
}