package cz.cvut.spipes.util;

import cz.cvut.spipes.spin.vocabulary.SP;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.topbraid.shacl.arq.SHACLFunctions;
import org.topbraid.shacl.validation.sparql.SPARQLSubstitutions;

import java.util.*;

public class SPipesUtil {

    private final static Set<String> systemFunctions = Collections.synchronizedSet(loadSystemFunctions());

    static {
        SPARQLSubstitutions.useGraphPrefixes = true;
    }

    private static Set<String> loadSystemFunctions(){
        Set<String> systemFunctions  = new HashSet<>();
        FunctionRegistry.init();
        Iterator<String> iter = FunctionRegistry.get().keys();
        while (iter.hasNext()) {
            String key = iter.next();
            systemFunctions.add(key);
        }
        return systemFunctions;
    }

    public static void init(){
        SP.getURI(); // load jena enh nodes (e.g. Construct and Select)
    }

    public static List<String> getNonSystemFunctions() {
        Iterator<String> iter = FunctionRegistry.get().keys();

        List<String> ret = new ArrayList<>();
        while(iter.hasNext()){
            String key = iter.next();
            if(!systemFunctions.contains(key))
                ret.add(key);
        }
        return ret;
    }

    /**
     * Reset only the functions declared in <code>model</code>. Only shacl rdf functions are loaded.
     * @param model
     */
    public static void resetFunctions(Model model){
        unregisterNonSystemFunctions();
        SHACLFunctions.registerFunctions(model);
    }

    private static void unregisterNonSystemFunctions(){
        for(String key: getNonSystemFunctions())
            FunctionRegistry.get().remove(key);
    }
}
