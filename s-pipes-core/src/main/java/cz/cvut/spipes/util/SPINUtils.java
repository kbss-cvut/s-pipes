package cz.cvut.spipes.util;

import cz.cvut.spipes.constants.SM;
import cz.cvut.spipes.constants.SMF;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.spin.system.SPINModuleRegistry;
import org.topbraid.spin.vocabulary.SP;
import org.topbraid.spin.vocabulary.SPL;
import org.topbraid.spin.vocabulary.SPR;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    public static List<String> getRegisteredCustomFunctions() {
        return SPINModuleRegistry.get().getFunctions().stream()
                .map(Resource::getURI)
                .filter(SPINUtils::startWithCustomPrefix)
                .collect(Collectors.toList());
    }

    private static boolean startWithCustomPrefix(String url) {
        return COMMON_PREFIXES.stream().noneMatch(url::startsWith);
    }
}
