package cz.cvut.spipes.logging;

import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.engine.ExecutionEngine;
import cz.cvut.spipes.engine.ExecutionEngineFactory;
import cz.cvut.spipes.modules.TestIdentityModule;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.Test;

public class LoggingTest {

    @Test public void testSingleRun() {
        singleRun();
    }

    private void singleRun() {
        final TestIdentityModule m = new TestIdentityModule();

        final Model model = ModelFactory.createDefaultModel();
        final Resource r = model.createResource();
        model.add(r, RDFS.label, "Test1");
        m.setConfigurationResource(r);

        final ExecutionEngine e = ExecutionEngineFactory.createEngine();
        final SemanticLoggingProgressListener l = new SemanticLoggingProgressListener();
        e.addProgressListener(l);
        e.executePipeline(m, ExecutionContextFactory.createEmptyContext());
        e.removeProgressListener(l);
    }

    private void logNoOp3() {
        for(int i = 0; i < 100;i++) {
            testSingleRun();
        }
    }

    @Test public void testThreadConcurrence() {
        final ExecutorService s = Executors.newFixedThreadPool(2);
        s.submit(() -> logNoOp3());
        s.submit(() -> logNoOp3());
        s.shutdown();
        try {
            s.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}