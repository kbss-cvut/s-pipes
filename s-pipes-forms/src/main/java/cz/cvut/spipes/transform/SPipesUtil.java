package cz.cvut.spipes.transform;

import cz.cvut.spipes.constants.SM;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.topbraid.spin.model.Ask;
import org.topbraid.spin.model.Construct;
import org.topbraid.spin.model.Describe;
import org.topbraid.spin.model.Select;

import java.util.HashSet;
import java.util.Set;

public class SPipesUtil {
    static final Class[] SPIN_QUERY_CLASSES = {Ask.class, Construct.class, Describe.class, Select.class};

    private static final Set<String> S_PIPES_TERMS = new HashSet<String>() {
        {
            add(SM.next.getURI());
            add(RDF.type.getURI());
        }
    };

    public static boolean isSPipesTerm(Resource term) {
        return S_PIPES_TERMS.contains(term.getURI());
    }

    public static boolean isSpinQuery(Resource r) {
        for (int i = 0; i < SPIN_QUERY_CLASSES.length; i++)
            if (r.canAs(SPIN_QUERY_CLASSES[i]))
                return true;
        return false;
    }
}
