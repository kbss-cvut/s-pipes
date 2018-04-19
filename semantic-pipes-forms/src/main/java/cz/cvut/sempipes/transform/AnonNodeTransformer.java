package cz.cvut.sempipes.transform;

import cz.cvut.sforms.Vocabulary;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.*;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.model.*;
import org.topbraid.spin.util.SPINExpressions;

/**
 * Created by Yan Doroshenko (yandoroshenko@protonmail.com) on 19.04.2018.
 */
public class AnonNodeTransformer {

    public static String serialize(RDFNode node) {
        Resource r = node.asResource();
        Property text = ResourceFactory.createProperty(Vocabulary.s_p_text);
        if (r.getProperty(text) != null) {
            return r.getProperty(text).getLiteral().getString();
        }
        else if (SPINExpressions.isExpression(r)) {
            return SPINFactory.asExpression(r).toString();
        }
        else if (r.canAs(Ask.class)) {
            return getFromQuery(r, Ask.class);
        }
        else if (r.canAs(Construct.class)) {
            return getFromQuery(r, Construct.class);
        }
        else if (r.canAs(Describe.class)) {
            return getFromQuery(r, Describe.class);
        }
        else if (r.canAs(Select.class)) {
            return getFromQuery(r, Select.class);
        }
        return "";
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
