package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import cz.cvut.sempipes.registry.StreamResource;
import cz.cvut.sempipes.registry.StringStreamResource;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class ModuleImportE5XTest {
    private String path;

    private String contentType;

    public ModuleImportE5XTest(String path, String contentType) {
        this.path = path;
        this.contentType = contentType;
    }

    public void execute() throws Exception {
        String e5xFilePath = ModuleImportE5XTest.class.getResource(path).getPath();
        StreamResource streamResouce = new StringStreamResource(e5xFilePath, Files.readAllBytes(Paths.get(e5xFilePath)), contentType);

        ModuleImportE5x module = new ModuleImportE5x();

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
    }
}
