package cz.cvut.sempipes.modules;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.*;
import java.util.List;

/**
 * Created by Miroslav Blasko on 28.11.16.
 */
@RunWith(Parameterized.class)
public class ModuleImportE5XZIPTest extends ModuleImportE5XTest {


    public ModuleImportE5XZIPTest(String path, String contentType) {
        super(path,contentType);
    }

    @Parameterized.Parameters
    public static Object[] generateTestData() {
        List<String> files = null;
        String dir = "data/e5x.xml";
        String contentType = "text/xml";

        try {
            files = IOUtils.readLines((ModuleImportE5XZIPTest.class.getClassLoader().getResourceAsStream(dir)), Charsets.UTF_8);
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