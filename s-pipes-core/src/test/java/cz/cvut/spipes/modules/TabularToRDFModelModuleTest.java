package cz.cvut.spipes.modules;

import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TabularToRDFModelModuleTest extends AbstractModuleTestHelper {

        @Override
        public String getModuleName() {
            return "tabular";
        }

        @Test
        public void executeWithSimpleTransformation() throws URISyntaxException {

            TabularToRDFModelModule module = new TabularToRDFModelModule();

            Path filePath = this.getFilePath("countries.tsv");

            module.setSourceFilePath(filePath.toString());
            module.setReplace(true);
            module.setDelimiter('\t');
            module.setDataPrefix("http://onto.fel.cvut.cz/data/");

            module.setInputContext(ExecutionContextFactory.createEmptyContext());

            ExecutionContext outputContext = module.executeSelf();

            assertTrue(outputContext.getDefaultModel().size() > 0);
        }

}