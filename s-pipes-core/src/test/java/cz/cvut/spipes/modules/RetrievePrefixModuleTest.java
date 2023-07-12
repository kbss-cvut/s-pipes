package cz.cvut.spipes.modules;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Map;

class RetrievePrefixModuleTest {

    @Test
    void testGetPrefixesFromFile() {
        ArrayList<String> scriptPaths = new ArrayList<>();
        scriptPaths.add("../../doc/examples/rdf4j-update/rdf4j-update.sms.ttl");
        scriptPaths.add("../../doc/examples/hello-world/hello-world.sms.ttl");
        RetrievePrefixModule retrievePrefixModule = new RetrievePrefixModule();
        retrievePrefixModule.retrieveFromScripts(scriptPaths);
        Map<String,String> prefixMap = retrievePrefixModule.getPrefixMap();
        System.out.println(prefixMap.size()+"\n"+prefixMap);
    }
}