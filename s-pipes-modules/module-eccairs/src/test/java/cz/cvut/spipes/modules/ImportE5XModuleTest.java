package cz.cvut.spipes.modules;

import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.registry.StreamResource;
import cz.cvut.spipes.registry.StringStreamResource;
import java.io.IOException;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import static org.aspectj.bridge.MessageUtil.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class ImportE5XModuleTest {

    public void execute(String path, String contentType) {
        URL e5xFileResource = ImportE5XModuleTest.class.getResource(path);
        String e5xFilePath = e5xFileResource.getPath();
        try {
            byte[] file = IOUtils.toByteArray(e5xFileResource);
            StreamResource streamResouce = new StringStreamResource(e5xFilePath, file, contentType);

            ImportE5XModule module = new ImportE5XModule();

            module.setInputContext(ExecutionContextFactory.createEmptyContext());
            module.setE5xResource(streamResouce);

            module.setComputeEccairsToAviationSafetyOntologyMapping(false);
            ExecutionContext outputContextWithoutMapping = module.executeSelf();
            long modelSizeWithoutMapping = outputContextWithoutMapping.getDefaultModel().size();

            module.setComputeEccairsToAviationSafetyOntologyMapping(true);
            ExecutionContext outputContextWithMapping = module.executeSelf();
            long modelSizeWithMapping = outputContextWithMapping.getDefaultModel().size();

            assertTrue(modelSizeWithoutMapping > 0);
            assertTrue(modelSizeWithoutMapping < modelSizeWithMapping);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }
}
