package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.TestConstants;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionEngine;
import cz.cvut.sempipes.engine.ExecutionEngineFactory;
import cz.cvut.sempipes.engine.PipelineFactory;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by Miroslav Blasko on 26.5.16.
 */
public class TarqlModuleTest {

    String TARQL_MODULE_DIR = "module/tarql";

    @Ignore
    @Test
    public void execute() throws Exception {

        ExecutionEngine e = ExecutionEngineFactory.createEngine();

        Module module = PipelineFactory.loadModule(
                TestConstants.TEST_RESOURCES_DIR_PATH
                        .resolve(TARQL_MODULE_DIR)
                        .resolve("spin-query-config.ttl"),
                "http://onto.fel.cvut.cz/ontologies/test/tarql#CreateSampleTriples");

        ExecutionContext ec = module.execute();
        ec.getDefaultModel().write(System.out);

    }

}