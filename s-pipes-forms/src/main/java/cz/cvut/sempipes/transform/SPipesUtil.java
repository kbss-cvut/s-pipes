package cz.cvut.sempipes.transform;

import cz.cvut.sempipes.constants.SM;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

public class SPipesUtil {

    private static final Set<String> S_PIPES_TERMS = new HashSet<String>() {
        {
            add(SM.next.getURI());
            add(RDF.type.getURI());
        }
    };

    public static boolean isSPipesTerm(Resource term) {
        return S_PIPES_TERMS.contains(term.getURI());
    }
}
