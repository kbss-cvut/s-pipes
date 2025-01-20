package cz.cvut.spipes.modules;

import cz.cvut.spipes.engine.ExecutionContextFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDFS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;



public class ApplyConstructWithChunkedValuesModuleTest extends AbstractModuleTestHelper {

    @BeforeEach
    public void setUp() {

    }

    @Override
    String getModuleName() {
        return "apply-construct-with-chunked-values";
    }

    @Test
    public void executeWithPreviousBinding()  {
        ApplyConstructWithChunkedValuesModule module = (ApplyConstructWithChunkedValuesModule) getConfigRootModule();

        module.setInputContext(ExecutionContextFactory.createContext(createSimpleModel()));
        module.loadConfiguration();

        module.executeSelf();
        assertEquals(module.getCurrentResultSetInstance().getResultVars().size(), 4);
    }

    @Test
    public void executeWithoutPreviousBinding()  {
        ApplyConstructWithChunkedValuesModule module = (ApplyConstructWithChunkedValuesModule) getConfigRootModule();

        module.setInputContext(ExecutionContextFactory.createContext(createSimpleModel()));
        module.loadConfiguration();
        module.setIsExtendSelectQueryResultWithPreviousBinding(false);

        module.executeSelf();
        assertEquals(module.getCurrentResultSetInstance().getResultVars().size(), 2);
    }


    private Model createSimpleModel() {
        Model model = ModelFactory.createDefaultModel();
        model.add(
                model.getResource("http://example.org"),
                RDFS.label,
                ResourceFactory.createPlainLiteral("illustration")
        );
        return model;
    }

}