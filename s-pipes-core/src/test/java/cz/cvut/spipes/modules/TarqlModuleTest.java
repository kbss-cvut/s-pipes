package cz.cvut.spipes.modules;

import cz.cvut.spipes.TestConstants;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionEngine;
import cz.cvut.spipes.engine.ExecutionEngineFactory;
import cz.cvut.spipes.engine.PipelineFactory;
import cz.cvut.spipes.util.JenaUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class TarqlModuleTest {

    String TARQL_MODULE_DIR = "module/tarql";

    @Disabled
    @Test
    public void execute() throws Exception {

        ExecutionEngine e = ExecutionEngineFactory.createEngine();

        Module module = PipelineFactory.loadModule(
                TestConstants.TEST_RESOURCES_DIR_PATH
                        .resolve(TARQL_MODULE_DIR)
                        .resolve("spin-query-config.ttl"),
                "http://onto.fel.cvut.cz/ontologies/test/tarql#CreateSampleTriples");

        ExecutionContext ec = module.execute();
        JenaUtils.write(ec.getDefaultModel());
    }

}