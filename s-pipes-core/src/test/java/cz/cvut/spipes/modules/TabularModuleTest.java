package cz.cvut.spipes.modules;

import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.util.StreamResourceUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TabularModuleTest extends AbstractModuleTestHelper {

        @Override
        public String getModuleName() {
            return "tabular";
        }

        @Test
        public void executeWithSimpleTransformation() throws URISyntaxException, IOException {

            TabularModule module = new TabularModule();

            module.setSourceResource(
                StreamResourceUtils.getStreamResource(
                    "http://test-file",
                    getFilePath("countries.tsv"))
            );
            module.setReplace(true);
            module.setDelimiter('\t');
            module.setDataPrefix("http://onto.fel.cvut.cz/data/");

            module.setInputContext(ExecutionContextFactory.createEmptyContext());

            ExecutionContext outputContext = module.executeSelf();

            assertTrue(outputContext.getDefaultModel().size() > 0);
        }

}