package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import cz.cvut.sempipes.registry.StreamResource;
import cz.cvut.sempipes.registry.StringStreamResource;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by Miroslav Blasko on 28.11.16.
 */
@RunWith(Parameterized.class)
public class ModuleImportE5XXMLTest extends ModuleImportE5XTest {

    public ModuleImportE5XXMLTest(String path, String contentType) {
        super(path, contentType);
    }

    @Parameterized.Parameters
    public static Object[] generateTestData() {
        String dir = "data/e5x.xml";
        String contentType = "text/xml";

        List<String> files = null;
        try {
            files = IOUtils.readLines((ModuleImportE5XXMLTest.class.getClassLoader().getResourceAsStream(dir)), Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Object[] data = files.stream().map((file) -> new Object[]{"/" + dir + "/" + file,  contentType}).toArray();
        return data;
    }

    @Override
    @Test
    public void execute() throws Exception {
        super.execute();
    }
}
