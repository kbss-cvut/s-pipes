package cz.cvut.sempipes.util;

import cz.cvut.sempipes.constants.SM;
import org.apache.commons.io.IOUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.util.JenaUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Miroslav Blasko on 18.5.16.
 */
public class JenaModuleUtils {

    private static final Logger LOG = LoggerFactory.getLogger(JenaModuleUtils.class);


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


    public static Map<Resource, Resource> getAllModulesWithTypes(Model config) {

        Query query = QueryFactory.create(loadResource("/query/get-all-modules.sparql"));
        QueryExecution queryExecution = QueryExecutionFactory.create(query, config);

        Map<Resource, Resource> module2moduleTypeMap = new HashMap<>();

        queryExecution.execSelect().forEachRemaining(
                qs -> {
                    Resource module = qs.get("module").asResource();
                    Resource moduleType = qs.get("moduleType").asResource();
                    Resource previous = module2moduleTypeMap.put(module, moduleType);
                    if (previous != null) {
                        LOG.error("Module {} has colliding module types -- {}, {}. Ignoring type {}.", module, previous, moduleType, previous);
                    }
                }
                    );
        return module2moduleTypeMap;
    }

    private static String loadResource(String path) {
        try (InputStream is = JenaModuleUtils.class.getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalArgumentException("Resource with path " + path + " not found.");
            }
            return IOUtils.toString(is);
        } catch (IOException e) {
           throw new IllegalArgumentException("Resource with path " + path + " could not be open.", e);
        }
    }

}
