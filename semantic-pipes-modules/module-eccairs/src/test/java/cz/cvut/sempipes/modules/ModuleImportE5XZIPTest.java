package cz.cvut.sempipes.modules;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.*;
import java.util.List;

@RunWith(Parameterized.class)
public class ModuleImportE5XZIPTest extends ModuleImportE5XTest {


    public ModuleImportE5XZIPTest(String path, String contentType) {
        super(path, contentType);
    }

    @Parameterized.Parameters
    public static Object[] generateTestData() {
        String dir = "data/e5x.zip";
        String contentType = "application/octet-stream";

        List<String> files = null;
        try {
            files = IOUtils.readLines((ModuleImportE5XZIPTest.class.getClassLoader().getResourceAsStream(dir)), Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return files.stream().map((file) -> new Object[]{"/" + dir + "/" + file, contentType}).toArray();
    }

    @Override
    @Test
    public void execute() {
        super.execute();
    }
}