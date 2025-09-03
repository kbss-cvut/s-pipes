package cz.cvut.spipes.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class EncodingUtils {
    private static final Logger log = LoggerFactory.getLogger(EncodingUtils.class);

    public static final String UTF8 = "UTF-8";
    
    public static String urlEncode(String str){
        return URLEncoder.encode(str, StandardCharsets.UTF_8);
    }
    
    public static String fileNameEncode(String str){
        return urlEncode(str);
    }
}
