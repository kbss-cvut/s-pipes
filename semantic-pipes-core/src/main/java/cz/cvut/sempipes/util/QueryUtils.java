package cz.cvut.sempipes.util;

import java.util.regex.Matcher;

public class QueryUtils {


    /**
     * Returns new query by substituting marker within given query with given value.
     * Marker must be in syntax #${MARKER_NAME}.
     * For example for marker with name "VALUES" query can look like following one :
     * SELECT * {
     *     #${VALUES}
     * }
     *
     * @param markerName name of the marker
     * @param replacedValue replacement of the marker
     * @param query query with the marker
     * @return new query with replaced value in place of the marker
     */
    public static String substituteMarkers(String markerName, String replacedValue, String query) {
        return query.replaceAll("\\s*#\\s*\\$\\{" + markerName + "\\}", Matcher.quoteReplacement(replacedValue));
    }
}
