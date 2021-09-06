package cz.cvut.spipes.modules;

import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TabularToRDFModelModuleTest extends AbstractModuleTestHelper {

        @Override
        public String getModuleName() {
            return "tabular";
        }

        @Test
        public void executeWithSimpleTransformation() {

            TabularToRDFModelModule module = new TabularToRDFModelModule();

            module.setSourceFilePath(this.getFilePath("countries.tsv"));
            module.setReplace(true);

            module.setInputContext(ExecutionContextFactory.createEmptyContext());

            ExecutionContext outputContext = module.executeSelf();

            assertTrue(outputContext.getDefaultModel().size() > 0);
        }

}