package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.TestConstants;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextImpl;
import cz.cvut.sempipes.engine.ExecutionEngine;
import org.junit.Test;

import java.nio.file.Paths;

/**
 * Created by Miroslav Blasko on 26.5.16.
 */
public class TarqlModuleTest {

    String TARQL_MODULE_DIR = "tarql-module";

    @Test
    public void execute() throws Exception {

        ExecutionEngine e = new ExecutionEngine();

        Module module = ModuleFactory.loadModule(
                Paths.get(TestConstants.RESOURCES_DIR_PATH,
                        TARQL_MODULE_DIR,
                        "spin-query-config.ttl"),
                "http://onto.fel.cvut.cz/ontologies/test/tarql-module#CreateSampleTriples");

        ExecutionContext ec = module.execute(new ExecutionContextImpl());
        ec.getDefaultModel().write(System.out);
    }

}