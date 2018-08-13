package cz.cvut.spipes.modules;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ImportE5XZIPModuleTest extends ImportE5XModuleTest {

    private static Stream<Arguments> generateTestData() {
        String dir = "data/e5x.zip";
        String contentType = "application/octet-stream";

        List<String> files = null;
        try {
            files = IOUtils.readLines((ImportE5XZIPModuleTest.class.getClassLoader().getResourceAsStream(dir)), Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return files.stream().map((file) -> Arguments.of("/" + dir + "/" + file, contentType));
    }

    @Override
    @Disabled
    @ParameterizedTest
    @MethodSource("generateTestData")
    public void execute(String path, String contentType) {
        super.execute(path, contentType);
    }
}