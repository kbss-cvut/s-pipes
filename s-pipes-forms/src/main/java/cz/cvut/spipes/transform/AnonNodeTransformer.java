package cz.cvut.spipes.transform;

import cz.cvut.sforms.Vocabulary;
import cz.cvut.sforms.model.Question;
import cz.cvut.spipes.spin.vocabulary.SP;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import java.util.Map;
import java.util.Optional;

public class AnonNodeTransformer {

    public static String serialize(RDFNode node) {
        Resource r = node.asResource();

        if (r.getProperty(SP.text) != null)
            return r.getProperty(SP.text).getObject().toString();

        // TODO - return "" or null when node is null?
        return Optional.ofNullable(node).map(RDFNode::toString).orElse("");
    }

    public static Query parse(Question q, Model m) {
        String t = q.getProperties().get(Vocabulary.s_p_has_answer_value_type).iterator().next();
        if(!SPipesUtil.isSpinQueryType(t))
            throw new UnsupportedOperationException();

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

}
