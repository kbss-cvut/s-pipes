package cz.cvut.spipes.manager;

import cz.cvut.spipes.engine.PipelineFactory;
import cz.cvut.spipes.exception.ResourceNotFoundException;
import cz.cvut.spipes.exception.ResourceNotUniqueException;
import cz.cvut.spipes.modules.Module;
import cz.cvut.spipes.registry.JenaResourceRegistry;
import cz.cvut.spipes.registry.ResourceRegistry;
import cz.cvut.spipes.repository.SMScriptCollectionRepository;
import cz.cvut.spipes.repository.ScriptCollectionRepository;
import cz.cvut.spipes.util.JenaPipelineUtils;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Registers resources to contexts.
 * <p>
 * resource -> location
 * alternative id -> location
 * <p>
 * TODO resource registry
 *
 */
public class SPipesScriptManager {

    private static final Logger LOG = LoggerFactory.getLogger(SPipesScriptManager.class);

    // TODO instead of ontoDocManager should point to ScriptCollectionRepository
    private Set<String> globalScripts;
    ///private final Map<String, OntModel> globalScriptsMap = new LinkedHashMap<>();
    private final ScriptCollectionRepository scriptsRepository;
    private ResourceRegistry pipelineFunctionRegistry;
    private ResourceRegistry moduleRegistry;
    private final OntologyDocumentManager ontoDocManager;

    private void registerAll(OntologyDocumentManager ontoDocManager, Collection<String> globalScripts) {
        List<Resource> pipelineFunctions = scriptsRepository.getPipelineFunctions(globalScripts);
        List<Resource> modules = scriptsRepository.getModules(globalScripts);

        pipelineFunctionRegistry = new JenaResourceRegistry(pipelineFunctions);
        moduleRegistry = new JenaResourceRegistry(modules);
    }

    public void reloadScripts(Collection<String> globalScript) {
        this.globalScripts = new HashSet<>(globalScript);
        registerAll(ontoDocManager, this.globalScripts);
    }

    public SPipesScriptManager(OntologyDocumentManager ontoDocManager, Collection<String> globalScripts) {
        this.ontoDocManager = ontoDocManager;
        scriptsRepository = new SMScriptCollectionRepository(ontoDocManager);

        this.globalScripts = new HashSet<>(globalScripts);

        registerAll(ontoDocManager, globalScripts);
    }


    public Module loadModule(final String moduleId,
                      final String moduleTypeUri,
                      final String contextUri) throws ResourceNotFoundException, ResourceNotUniqueException {

        // instantiate module type
        if (moduleId == null) {
            throw new UnsupportedOperationException();
        }

        String resourceContextUri = getModuleLocation(moduleId, contextUri);

        // TODO check moduleTypeUri
        String resourceUri = moduleRegistry.getResourceUri(moduleId);
        return PipelineFactory.loadModule(scriptsRepository.getResource(resourceUri, resourceContextUri));
    }

    public Module loadFunction(String functionId) {

        // TODO interface to return URI+Context would be more appropriate (jena*.Resource ?)
        String resourceUri = pipelineFunctionRegistry.getResourceUri(functionId);
        String resourceContextUri = pipelineFunctionRegistry.getContexts(resourceUri).iterator().next();
        Resource functionRes = scriptsRepository.getResource(resourceUri, resourceContextUri);

        Resource returnModuleRes = getReturnModule(functionRes);
        return PipelineFactory.loadModulePipeline(returnModuleRes);
    }

    public OntModel getScriptByContextId(String contextId){
        return scriptsRepository.getContextClosure(contextId);
    }


    //TODO !!!! should not be implemented here
    private Resource getReturnModule(Resource functionResource) {
        return JenaPipelineUtils.getAllFunctionsWithReturnModules(functionResource.getModel()).get(functionResource);
    }

    public OntologyDocumentManager getOntoDocManager() {
        return ontoDocManager;
    }

    public Set<String> getGlobalScripts() {
        return globalScripts;
    }


    /**
     *
     * @param moduleId
     * @param contextUri
     * @return path to file containing the module identified by <code>moduleId</code> if <code>contextUri</code> is null
     * otherwise the path to the file containing <code>contextUri</code>
     */
    public String getModuleLocation(final String moduleId, final String contextUri) {
        String resourceContextUri;
        // find existing module
        if (contextUri == null) {
            Set<String> iris = moduleRegistry.getContexts(moduleId);
            if(iris.isEmpty()){
                throw new ResourceNotFoundException("Cannot find script for module with id \"%s\"".formatted(moduleId));
            }
            if(iris.size() > 1){
                throw new ResourceNotUniqueException((
                        "Cannot find location of module with id \"%s\", module found in multiple scripts: \"%s\"." +
                                "Set `_pScriptURI` parameter to locate module in a specific script. "
                ).formatted(moduleId, iris));
            }

            resourceContextUri = moduleRegistry.getContexts(moduleId).iterator().next();
        } else {
            resourceContextUri = contextUri;
        }
        return getLocation(resourceContextUri);
    }

    /**
     *
     * @param functionId
     * @return the location of the file containing the function identified by <code>functionId</code>
     */
    public String getFunctionLocation(final String functionId) {
        String resourceUri = pipelineFunctionRegistry.getResourceUri(functionId);
        String resourceContextUri = pipelineFunctionRegistry.getContexts(resourceUri).iterator().next();
        return getLocation(resourceContextUri);
    }

    /**
     * @implNote Based on jena's OntDocumentManager.
     * @param ontologyIRI
     * @return an absolute URI string at which <code>ontologyIRI</code> is loaded
     */
    public String getLocation(String ontologyIRI) {
        String scriptPath = OntDocumentManager.getInstance().getFileManager().mapURI(ontologyIRI);
        return Paths.get(scriptPath).toUri().toString();
    }
    // id -> contexts
    // function id-s



    // structure
        // map string -> list of strings
            // key
                // localname, prefix:localname, uri

    // operations

    // addValue();
    // addUniqueValue();


    // support for


//    public void findModule(String moduleId) {
//
//    }


    // ----------------------------------- PRIVATE METHODS -----------------------------------

}
