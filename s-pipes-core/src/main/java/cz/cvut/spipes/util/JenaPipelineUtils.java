package cz.cvut.spipes.util;

import cz.cvut.spipes.constants.SM;
import cz.cvut.spipes.manager.OntoDocManager;
import org.apache.commons.io.IOUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JenaPipelineUtils {

    private static final Logger log = LoggerFactory.getLogger(JenaPipelineUtils.class);


    public static boolean isModule(Resource res) {

        return res.listProperties(RDF.type).filterKeep(
                st -> {
                    if (!st.getObject().isResource()) {
                        return false;
                    }
                    Resource objRes = st.getObject().asResource();
                    return objRes.hasProperty(RDF.type, SM.JENA.Module);
                }
        ).toList().size() > 0;
    }


    public static Map<Resource, Resource> getAllModulesWithTypes(Model config) {

        Query query = QueryFactory.create(loadResource("/query/get-all-sm-modules.sparql"));
        QueryExecution queryExecution = QueryExecutionFactory.create(query, config);

        Map<Resource, Resource> module2moduleTypeMap = new HashMap<>();

        queryExecution.execSelect().forEachRemaining(
                qs -> {
                    Resource module = qs.get("module").asResource();
                    Resource moduleType = qs.get("moduleType").asResource();
                    Resource previous = module2moduleTypeMap.put(module, moduleType);
                    if (previous != null) {
                        log.error("Module {} has colliding module types -- {}, {}. Ignoring type {}.", module, previous, moduleType, previous);
                    }
                }
                    );
        return module2moduleTypeMap;
    }

    public static Map<Resource, Resource> getAllFunctionsWithReturnModules(Model config) {
        Query query = QueryFactory.create(loadResource("/query/get-all-sm-functions.sparql"));
        QueryExecution queryExecution = QueryExecutionFactory.create(query, config);

        Map<Resource, Resource> function2retModuleMap = new HashMap<>();

        queryExecution.execSelect().forEachRemaining(
                qs -> {
                    Resource module = qs.get("function").asResource();
                    if (qs.get("returnModule") == null) {
                        //TODO cleaner workaround for -- ?function = <http://topbraid.org/sparqlmotion#Functions> )
                        return;
                    }
                    Resource moduleType = qs.get("returnModule").asResource();
                    log.debug("Registering function {} to return module {}.", module, moduleType);
                    Resource previous = function2retModuleMap.put(module, moduleType);
                    if (previous != null) {
                        log.error("Function {} has colliding return modules -- {}, {}. Ignoring type {}.", module, previous, moduleType, previous);
                    }
                }
        );
        return function2retModuleMap;
    }

    //TODO
    public static List<Resource> findPipelineModules(Resource rootModule) {
        OntModel ontModel = loadLibrary();

//        Template template = SPINModuleRegistry.get().getTemplate(SEMP_LIB.get_pipeline_modules.toString(), ontModel);
//
//        TemplateCall templateCall = SPINFactory.createTemplateCall(ModelFactory.createDefaultModel(), template.asResource());
//
//
//
//        ontModel.getResource(SEMP_LIB.get_pipeline_modules.getURI()).as(Select.class);
//
//
//        Query query = ARQFactory.get().createQuery(selectQuery);
//
//        QueryExecution execution = QueryExecutionFactory.create(query, executionContext.getDefaultModel());
//
//        QuerySolution qs = execution.execSelect().next();
//
//        VariablesBinding variablesBinding = new VariablesBinding(qs);
        return null;
    }

    private static String loadResource(String path) {
        try (InputStream is = JenaPipelineUtils.class.getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalArgumentException("Resource with path " + path + " not found.");
            }
            return IOUtils.toString(is);
        } catch (IOException e) {
           throw new IllegalArgumentException("Resource with path " + path + " could not be open.", e);
        }
    }


    private static OntModel loadLibrary() {
        return OntoDocManager.loadOntModel("/lib.ttl");
    }




}
