package cz.cvut.spipes.modules;

import cz.cvut.spipes.engine.ExecutionContextFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;



public class ApplyConstructWithChunkedValuesModuleTest extends AbstractSparqlEndpointModuleTestHelper {

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

        module.setInputContext(ExecutionContextFactory.createEmptyContext());
        module.loadConfiguration();

        module.executeSelf();
        assertEquals(4, module.getCurrentResultSetInstance().getResultVars().size());
    }

    @Test
    public void executeWithoutPreviousBinding()  {
        ApplyConstructWithChunkedValuesModule module = (ApplyConstructWithChunkedValuesModule) getConfigRootModule();

        module.setInputContext(ExecutionContextFactory.createEmptyContext());
        module.loadConfiguration();
        module.setIsExtendSelectQueryResultWithPreviousBinding(false);

        module.executeSelf();
        assertEquals(2, module.getCurrentResultSetInstance().getResultVars().size());
    }
}