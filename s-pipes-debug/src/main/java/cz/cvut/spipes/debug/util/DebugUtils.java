package cz.cvut.spipes.debug.util;

import static cz.cvut.spipes.Vocabulary.s_c_transformation;

public class DebugUtils {
    public static String getExecutionIdFromIri(String iri) {
        int startIndex = iri.lastIndexOf("/") + 1;
        int endIndex = iri.length();
        return iri.substring(startIndex, endIndex);
    }

    public static String getTransformationIriFromId(String executionId){
        return s_c_transformation + "/" + executionId;
    }
}
