package cz.cvut.spipes.util;

import cz.cvut.spipes.config.ContextLoaderConfig;
import cz.cvut.spipes.manager.OntoDocManager;
import cz.cvut.spipes.manager.OntologyDocumentManager;
import cz.cvut.spipes.manager.SPipesScriptManager;
import cz.cvut.spipes.util.CoreConfigProperies;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.util.LocationMapper;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class ContextLoaderHelper {

    // TODO should not point to scriptManager

    /**
     * Reloads those contexts (i.e. files) whose time stamp is newer than the time stamp of the last reload.
     * @param scriptManager
     */
    public static void updateContextsIfNecessary(SPipesScriptManager scriptManager) {
        if (isKeepUpdated()) {
            log.warn("Updating contexts which is not thread safe -- don't use in in production environment.");
            OntologyDocumentManager ontoDocManager = OntoDocManager.getInstance();
            OntoDocManager.setReloadFiles(true);
            List<String> globalScripts = ContextLoaderHelper.registerGlobalScripts(ontoDocManager);
            scriptManager.reloadScripts(globalScripts);
        }
    }

    /**
     * Registers all scripts from <code>contexts.scriptPaths</code> variable and return those files that
     * represents global scripts (i.e. ending with sms.ttl).
     *
     * @param ontDocManager Ontology document manager to register the scripts.
     * @return List of baseIRIs of global scripts.
     */
    public static List<String> registerGlobalScripts(OntologyDocumentManager ontDocManager) {
        List<Path> scriptPaths = ContextLoaderConfig.getScriptPaths();
        scriptPaths.forEach(
                ontDocManager::registerDocuments
        );

        LocationMapper locMapper = ontDocManager.getOntDocumentManager().getFileManager().getLocationMapper();

        List<String> _globalScripts = new LinkedList<>();

        locMapper.listAltEntries().forEachRemaining(
                ontoUri -> {
                    String loc = locMapper.getAltEntry(ontoUri);
                    if (loc.endsWith(".sms.ttl")) {
                        log.info("Registering script from file " + loc + ".");
                        _globalScripts.add(ontoUri);
                    }
                }
        );
        return _globalScripts;
    }

    public static boolean isKeepUpdated() {
        return Boolean.parseBoolean(CoreConfigProperies.get("contextsLoader.data.keepUpdated"));
    }

}
