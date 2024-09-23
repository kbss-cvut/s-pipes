package cz.cvut.spipes.rest.handler;

import java.util.List;

/**
* Represents XSD datatypes used in JSON-LD without requiring explicit "@type" declaration.
**/
public class JsonLdCoreLiteralDatatypes {

    public static final String XSD_NAMESPACE = "http://www.w3.org/2001/XMLSchema#";

    public static final String XSD_STRING = XSD_NAMESPACE + "string";
    public static final String XSD_INTEGER = XSD_NAMESPACE + "integer";
    public static final String XSD_BOOLEAN = XSD_NAMESPACE + "boolean";
    public static final String XSD_DOUBLE = XSD_NAMESPACE + "double";

    public static final List<String> CORE_TYPES = List.of(XSD_STRING, XSD_INTEGER, XSD_BOOLEAN, XSD_DOUBLE);

    public static boolean isCorePrimitiveType(String type) {
        if(type == null) {
            return false;
        }
        return CORE_TYPES.contains(type);
    }
}
