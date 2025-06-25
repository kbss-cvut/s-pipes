package cz.cvut.spipes.spin.util;

import java.util.HashMap;
import java.util.Map;

public class ExtraPrefixes {
    private static Map<String,String> map = new HashMap<String,String>();

    static {
        map.put("afn", "http://jena.hpl.hp.com/ARQ/function#");
        map.put("fn", "http://www.w3.org/2005/xpath-functions#");
        map.put("jfn", "java:org.apache.jena.sparql.function.library.");
        map.put("pf", "http://jena.hpl.hp.com/ARQ/property#");
        map.put("smf", "http://topbraid.org/sparqlmotionfunctions#");
        map.put("tops", "http://www.topbraid.org/tops#");
    }

    public static Map<String,String> getExtraPrefixes() {
        return map;
    }
}
