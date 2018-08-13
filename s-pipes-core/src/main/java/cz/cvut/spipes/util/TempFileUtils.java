package cz.cvut.spipes.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import org.jetbrains.annotations.NotNull;


public class TempFileUtils {
    
    public static String createTimestampFileName(String suffix){
        String dirName = Instant.now() + suffix;
        return EncodingUtils.fileNameEncode(getWindowsFilesystemCompliantString(dirName));
    }
    
    public static Path createTempDirectory(String suffix) throws IOException{
        String dirName = createTimestampFileName(suffix);
        return Files.createTempDirectory(dirName);
    }

    private static String getWindowsFilesystemCompliantString(@NotNull String fileName) {
        return fileName.replace(':', '_');
    }
    
    
}
