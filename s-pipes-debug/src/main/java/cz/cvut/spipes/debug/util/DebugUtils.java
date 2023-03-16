package cz.cvut.spipes.debug.util;

public class DebugUtils {
    public static String getExecutionIdFromIri(String iri){
        int startIndex = iri.lastIndexOf("/") + 1;
        int endIndex = iri.length();
        return iri.substring(startIndex, endIndex);
    }
}
