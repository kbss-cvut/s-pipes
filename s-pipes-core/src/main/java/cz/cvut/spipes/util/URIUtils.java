package cz.cvut.spipes.util;

public class URIUtils {

    public static String getLocalName(String uri) {

        int lastSlashIndex = uri.lastIndexOf('/');
        int lastHashIndex = uri.lastIndexOf('#');

        int lastIndex = Math.max(lastSlashIndex, lastHashIndex);

        // Return substring after last '/' or '#'
        if (lastIndex != -1 && lastIndex + 1 < uri.length()) {
            return uri.substring(lastIndex + 1);
        }

        return uri;
    }
}
