package cz.cvut.spipes.util;

import cz.cvut.spipes.constants.SM;
import cz.cvut.spipes.constants.SMF;
import org.topbraid.spin.vocabulary.SP;
import org.topbraid.spin.vocabulary.SPL;
import org.topbraid.spin.vocabulary.SPR;
import org.apache.jena.sparql.function.FunctionRegistry;

import java.util.*;

public class SPINUtils {
    private static final Set<String> COMMON_PREFIXES = new HashSet<String>() {{
        add("http://jena.hpl.hp.com/ARQ/function#");
        add(SP.NS);
        add("http://spinrdf.org/spif#");
        add("http://spinrdf.org/spin#");
        add(SPL.NS);
        add(SPR.NS);
        add(SM.uri);
        add(SMF.uri);
        add("http://www.w3.org/2005/xpath-functions#");
    }};

    // TODO - unify api with SPipesUtil.getNonSystemFunctions
    public static List<String> getRegisteredCustomFunctions() {
        List<String> ret = new ArrayList<>();
        Iterator<String> iter = FunctionRegistry.get().keys();
        while (iter.hasNext()){
            String key = iter.next();
            if(startWithCustomPrefix(key))
                ret.add(key);
        }
        return ret;
    }

    private static boolean startWithCustomPrefix(String url) {
        return COMMON_PREFIXES.stream().noneMatch(url::startsWith);
    }
}
