package cz.cvut.sempipes.util;

import cz.cvut.sempipes.constants.SM;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

/**
 * Created by Miroslav Blasko on 18.5.16.
 */
public class JenaModuleUtils {
    public static boolean isModule(Resource res) {

        return res.listProperties(RDF.type).filterKeep(
                st -> {
                    if (!st.getObject().isResource()) {
                        return false;
                    }
                    Resource objRes = st.getObject().asResource();
                    return objRes.hasProperty(RDF.type, SM.Module);
                }
        ).toList().size() > 0;
    }
}
