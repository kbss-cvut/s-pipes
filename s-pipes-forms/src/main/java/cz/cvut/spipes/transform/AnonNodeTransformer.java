package cz.cvut.spipes.transform;

import cz.cvut.sforms.Vocabulary;
import cz.cvut.sforms.model.Question;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.model.*;
import org.topbraid.spin.util.SPINExpressions;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Map;

/**
 * Created by Yan Doroshenko (yandoroshenko@protonmail.com) on 19.04.2018.
 */
public class AnonNodeTransformer {

    private static final Class[] SPIN_QUERY_CLASSES = {Ask.class, Construct.class, Describe.class, Select.class};

    public static String serialize(RDFNode node) {
        Resource r = node.asResource();
        Property text = ResourceFactory.createProperty(Vocabulary.s_p_text);

        if (r.getProperty(text) != null) {
            return r.getProperty(text).getLiteral().getString();
        }
        if (SPINExpressions.isExpression(r)) {
            return SPINFactory.asExpression(r).toString();
        }
        for (Class c : SPIN_QUERY_CLASSES)
            if (r.canAs(c))
                return getFromQuery(r, c);
        return ARQFactory.get().createExpressionString(r);
    }

    public static Query parse(Question q, Model m) {
        String t = q.getProperties().get(Vocabulary.s_p_has_answer_value_type).iterator().next();
        switch (t) {
            case Vocabulary.s_c_Ask:
                break;
            case Vocabulary.s_c_Construct:
                break;
            case Vocabulary.s_c_Describe:
                break;
            case Vocabulary.s_c_Select:
                break;
            default:
                throw new NotImplementedException();
        }
        StringBuilder b = new StringBuilder();
        Map<String, String> map = m.getNsPrefixMap();
        String s = q.getAnswers().iterator().next().getTextValue();
        map.forEach((k, v) -> {
            if (s.contains(k))
                b.append(String.format("PREFIX %s: <%s>\n", k, v));
        });
        b.append(s);
        return QueryFactory.create(b.toString());
    }

    private static <T extends org.topbraid.spin.model.Query> String getFromQuery(Resource r, Class<T> resClass) {
        Query q = ARQFactory.get().createQuery(r.as(resClass));
        Model m = r.getModel();
        PrefixMapping mapping = new PrefixMappingImpl();
        mapping.setNsPrefixes(m.getNsPrefixMap());
        q.setPrefixMapping(mapping);
        return q.serialize().replaceAll("(?m)^PREFIX.*\n", "").trim();
    }
}
