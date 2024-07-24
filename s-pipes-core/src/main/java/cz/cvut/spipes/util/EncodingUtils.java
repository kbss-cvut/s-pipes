package cz.cvut.spipes.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class EncodingUtils {
    private static final Logger log = LoggerFactory.getLogger(EncodingUtils.class);

    public static final String UTF8 = "UTF-8";
    
    public static String urlEncode(String str){
        try{
            return URLEncoder.encode(str, UTF8);
        } catch (UnsupportedEncodingException ex) {
            log.error(String.format("Encoding {} is not supported.", UTF8),ex);
            throw new RuntimeException(ex);
        }
    }
    
    public static String fileNameEncode(String str){
        return urlEncode(str);
    }
}
