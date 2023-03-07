package cz.cvut.spipes.debug.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import cz.cvut.spipes.debug.model.ModuleExecution;
import cz.cvut.spipes.debug.persistance.dao.TransformationDao;
import cz.cvut.spipes.debug.tree.ExecutionTree;
import cz.cvut.spipes.manager.SPipesScriptManager;
import cz.cvut.spipes.modules.Module;
import cz.cvut.spipes.util.ScriptManagerFactory;


@Service
public class ScriptService {
    private final SPipesScriptManager scriptManager;

    private final TransformationDao transformationDao;

    private final DebugService debugService;

    public ScriptService(TransformationDao transformationDao, DebugService debugService) {
        this.transformationDao = transformationDao;
        this.debugService = debugService;
        scriptManager = ScriptManagerFactory.getSingletonSPipesScriptManager();
    }

    public void a(String id) {
        Module module = scriptManager.loadFunction(id);
        int depth = 0;
    }

    //    private Module recursiveFindTriple(int depth, Module module) {
    //        if (module.getInputModules().size() > 0) {
    //            module.getInputModules().forEach(m -> {
    //                recursiveFindTriple(depth + 1, m);
    //            });
    //        }
    //
    //    }

    /*
        SELECT ?subject ?predicate ?object ?context
        WHERE {
        GRAPH ?context {
            ?subject ?predicate ?object
            FILTER(REGEX(str(?context), "^http://onto.fel.cvut.cz/ontologies/dataset-descriptor/transformation/1678036284039000.*output$"))
            FILTER(?subject = <http://onto.fel.cvut.cz/ontologies/s-pipes/hello-world-example-0.1/aaaa-bbbbb>)
            FILTER(?predicate = <http://onto.fel.cvut.cz/ontologies/s-pipes/hello-world-example-0.1/is-greeted-by-message>)
            FILTER(?object = "Hello AAAA bbbbb.")
  }
}

        SELECT ?s ?p ?o
        WHERE {
          GRAPH <http://onto.fel.cvut.cz/ontologies/dataset-descriptor/transformation/1678036284039000/script> {
            ?s ?p ?o .
            FILTER (?p = <http://topbraid.org/sparqlmotion#next>)
          }
        }
     */
    public List<ModuleExecution> findTripleOrigin(String executionId, String pattern) {
        List<String> context = transformationDao.findContexts("whatever");
        List<String> iris = context.stream()
                .map(i -> i.replace("/output", ""))
                .collect(Collectors.toList());
        List<ModuleExecution> moduleExecutions = debugService.getAllModulesForExecution("1678036284039000", null);
        ExecutionTree executionTree = new ExecutionTree(moduleExecutions);
        System.out.println(context);
        return null;
    }
}

//        Repository repository = new HTTPRepository("http://localhost:8081/rdf4j-server", "s-pipes-hello-world");
//        RepositoryConnection con = repository.getConnection();
//        Model jenaModel = ModelFactory.createDefaultModel();
//        IRI context = con.getValueFactory().createIRI("http://onto.fel.cvut.cz/ontologies/dataset-descriptor/transformation/1678036284039000/script");
//        con.getStatements(null, null, null, context).forEach(statement -> {
//            Resource subject = ResourceFactory.createResource(statement.getSubject().stringValue());
//            Property predicate = ResourceFactory.createProperty(statement.getPredicate().stringValue());
//            RDFNode object = null;
//
//            if (statement.getObject() instanceof Literal) {
//                Literal literal = (Literal) statement.getObject();
//                object = ResourceFactory.createTypedLiteral(literal.getValue().toString(), literal.getDatatype());
//            } else {
//                object = ResourceFactory.createResource(statement.getObject().stringValue());
//            }
//
//            jenaModel.add(subject, predicate, object);
//        });
//        con.close();
//        System.out.println(jenaModel);
//        Resource resource = jenaModel.getResource("http://onto.fel.cvut.cz/ontologies/dataset-descriptor/transformation/1678036284039000/script");
//        Resource module = JenaPipelineUtils.getAllFunctionsWithReturnModules(resource.getModel()).get(resource);
//        System.out.println(module);
//1 Get Script Rdf4j Model
//2 Convert it to jena model
//3 Get List<Module/Resoruce>, so i can have next modules
//4 Check if output of the modules contain given triple from big depth
//5