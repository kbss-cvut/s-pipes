package cz.cvut.sempipes.manager;

import cz.cvut.sempipes.engine.PipelineFactory;
import cz.cvut.sempipes.exception.ResourceNotFoundException;
import cz.cvut.sempipes.exception.ResourceNotUniqueException;
import cz.cvut.sempipes.modules.Module;
import cz.cvut.sempipes.registry.JenaResourceRegistry;
import cz.cvut.sempipes.registry.ResourceRegistry;
import cz.cvut.sempipes.repository.SMScriptCollectionRepository;
import cz.cvut.sempipes.repository.ScriptCollectionRepository;
import cz.cvut.sempipes.util.JenaPipelineUtils;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.LocationMapper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Registers resources to contexts.
 *
 * resource -> location
 * alternative id -> location
 *
 * TODO resource registry
 *
 *
 *
 * Created by Miroslav Blasko on 22.7.16.
 */
public class SempipesScriptManager {

    private static final Logger LOG = LoggerFactory.getLogger(SempipesScriptManager.class);

    // TODO instead of ontoDocManager should point to ScriptCollectionRepository
    private Set<String> globalScripts;
    ///private final Map<String, OntModel> globalScriptsMap = new LinkedHashMap<>();
    //private final OntologyDocumentManager ontoDocManager;
    private final ScriptCollectionRepository scriptsRepository;
    private final ResourceRegistry functionRegistry;
    private final ResourceRegistry moduleRegistry;
    private OntologyDocumentManager ontoDocManager;

    public SempipesScriptManager(OntologyDocumentManager ontoDocManager, Collection<String> globalScripts) {

        //this.ontoDocManager = ontoDocManager;

//        globalScripts.stream().forEach(scriptCtx -> {
//            globalScriptsMap.put(scriptCtx, ontoDocManager.getOntology(scriptCtx));
//        });


        this.ontoDocManager = ontoDocManager;
        scriptsRepository = new SMScriptCollectionRepository(ontoDocManager);

        this.globalScripts = new HashSet<>(globalScripts);

        List<Resource> functions = scriptsRepository.getFunctions(globalScripts);
        List<Resource> modules = scriptsRepository.getModules(globalScripts);

        functionRegistry = new JenaResourceRegistry(functions);
        moduleRegistry = new JenaResourceRegistry(modules);
    }

    // TODO !!! move functionality to separate class
    public static List<String> getGlobalScripts(OntologyDocumentManager ontDocManager, Collection<Path> scriptDirs) {
        scriptDirs.forEach(
                ontDocManager::registerDocuments
        );

        LocationMapper locMapper = ontDocManager.getOntDocumentManager().getFileManager().getLocationMapper();

        List<String> _globalScripts = new LinkedList<>();

        locMapper.listAltEntries().forEachRemaining(
                ontoUri -> {
                    String loc = locMapper.getAltEntry(ontoUri);
                    if (loc.endsWith(".sms.ttl")) {
                        LOG.info("Registering script from file " + loc + ".");
                        _globalScripts.add(ontoUri);
                    }
                }
        );
        return _globalScripts;
    }


    public Module loadModule(final String moduleId,
                      final String moduleTypeUri,
                      final String contextUri) throws ResourceNotFoundException, ResourceNotUniqueException {

        // instantiate module type
        if (moduleId == null) {
            throw new NotImplementedException();
        }

        String resourceUri;
        String resourceContextUri;

        // find existing module
        if (contextUri == null) {
            resourceUri = moduleRegistry.getResourceUri(moduleId);
            resourceContextUri = moduleRegistry.getContexts(moduleId).iterator().next();
        } else {
            resourceUri = moduleRegistry.getResourceUri(moduleId, contextUri);
            resourceContextUri = contextUri;
        }

        // TODO check moduleTypeUri

        return PipelineFactory.loadPipeline(scriptsRepository.getResource(resourceUri, resourceContextUri));
    }




    public Module loadFunction(String functionId) {

        // TODO interface to return URI+Context would be more appropriate (jena*.Resource ?)
        String resourceUri = functionRegistry.getResourceUri(functionId);
        String resourceContextUri = functionRegistry.getContexts(resourceUri).iterator().next();
        Resource functionRes = scriptsRepository.getResource(resourceUri, resourceContextUri);

        Resource returnModuleRes = getReturnModule(functionRes);
        return PipelineFactory.loadModulePipeline(returnModuleRes);
    }


    //TODO !!!! shold not be implemented here
    private Resource getReturnModule(Resource functionResource) {
        return JenaPipelineUtils.getAllFunctionsWithReturnModules(functionResource.getModel()).get(functionResource);
    }

    public OntologyDocumentManager getOntoDocManager() {
        return ontoDocManager;
    }

    public Set<String> getGlobalScripts() {
        return globalScripts;
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
