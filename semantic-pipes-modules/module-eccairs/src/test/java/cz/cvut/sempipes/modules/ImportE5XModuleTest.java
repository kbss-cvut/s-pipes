package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import cz.cvut.sempipes.registry.StreamResource;
import cz.cvut.sempipes.registry.StringStreamResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

abstract class ImportE5XModuleTest {
    private String path;

    private String contentType;

    public ImportE5XModuleTest(String path, String contentType) {
        this.path = path;
        this.contentType = contentType;
    }

    public void execute() {
        String e5xFilePath = ImportE5XModuleTest.class.getResource(path).getPath();
        try {
            byte[] file = Files.readAllBytes(Paths.get(e5xFilePath));
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
