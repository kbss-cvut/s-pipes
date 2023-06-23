package cz.cvut.spipes.modules;

import cz.cvut.spipes.engine.ExecutionContext;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class Rdf4jUpdateModuleTest {

    @Test
    @Disabled
    void executeSelf() {
        Rdf4jUpdateModule module = new Rdf4jUpdateModule();
        ExecutionContext newContext = module.executeSelf();
//        System.out.println(newContext.getDefaultModel().listStatements().toList());
    }
}