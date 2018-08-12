package cz.cvut.spipes.transform;

import cz.cvut.sforms.Vocabulary;
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

    enum SpinQueries {
        ASK(Ask.class, Vocabulary.s_c_Ask),
        CONSTRUCT(Construct.class, Vocabulary.s_c_Construct),
        DESCRIBE(Describe.class, Vocabulary.s_c_Describe),
        SELECT(Select.class, Vocabulary.s_c_Select);

        private Class clazz;
        private String uri;

        SpinQueries(Class clazz, String uri) {
            this.clazz = clazz;
            this.uri = uri;
        }

        public Class getClazz() {
            return clazz;
        }

        public String getUri() {
            return uri;
        }
    }

    private static final Set<String> S_PIPES_TERMS = new HashSet<String>() {
        {
            add(SM.next.getURI());
            add(RDF.type.getURI());
        }
    };

    public static boolean isSPipesTerm(Resource term) {
        return S_PIPES_TERMS.contains(term.getURI());
    }

    public static String getSpinQueryUri(Resource r) {
        for (SpinQueries v : SpinQueries.values())
            if (r.canAs(v.getClazz()))
                return v.getUri();
        return null;
    }
}
