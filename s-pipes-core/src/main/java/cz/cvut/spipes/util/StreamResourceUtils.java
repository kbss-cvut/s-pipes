package cz.cvut.spipes.util;

import cz.cvut.spipes.registry.StreamResource;
import cz.cvut.spipes.registry.StringStreamResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class StreamResourceUtils {
    public static StreamResource getStreamResource(String uri, Path filePath) throws IOException {
        return new StringStreamResource(uri, Files.readAllBytes(filePath), null);
    }
}
