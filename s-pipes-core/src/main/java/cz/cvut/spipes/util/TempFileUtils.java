package cz.cvut.spipes.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;


public class TempFileUtils {
    
    public static String createTimestampFileName(String postfix){
        String dirName = Instant.now() + postfix;
        // make windows safe
//        return EncodingUtils.fileNameEncode(dirName);
        return EncodingUtils.fileNameEncode(dirName.replace(':', '_'));
    }
    
    public static Path createTempDirectory(String postfix) throws IOException{
        String dirName = createTimestampFileName(postfix);
        return Files.createTempDirectory(dirName);
    }
    
    
}
