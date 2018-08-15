package cz.cvut.spipes.modules;


import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import org.apache.jena.rdf.model.Model;
import org.junit.jupiter.api.Test;
import util.JenaTestUtils;

public class ConstructTextSerializationModuleTest {

    @Test
    public void executeSelf() {

        ConstructTextSerializationModule module = new ConstructTextSerializationModule();

        Model inputModel = JenaTestUtils.laodModelFromResource("/sample-form.ttl");

        ExecutionContext inputEC = ExecutionContextFactory.createContext(inputModel);

        module.setInputContext(inputEC);

        ExecutionContext outputEC = module.executeSelf();

        Model outputModel = outputEC.getDefaultModel();

    }
}
