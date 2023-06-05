package cz.cvut.spipes.util;

import cz.cvut.spipes.config.ContextLoaderConfig;
import cz.cvut.spipes.manager.OntoDocManager;
import cz.cvut.spipes.manager.OntologyDocumentManager;
import cz.cvut.spipes.manager.SPipesScriptManager;
import cz.cvut.spipes.util.CoreConfigProperies;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import org.apache.jena.util.LocationMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextLoaderHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ContextLoaderHelper.class);

    // TODO should not point to scriptManager
    public static void updateContextsIfNecessary(SPipesScriptManager scriptManager) {
        if (isKeepUpdated()) {
            LOG.warn("Updating contexts which is not thread safe -- don't use in in production environment.");
            OntologyDocumentManager ontoDocManager = OntoDocManager.getInstance();
            OntoDocManager.setReloadFiles(true);
            List<String> globalScripts = ContextLoaderHelper.registerGlobalScripts(ontoDocManager);
            scriptManager.reloadScripts(globalScripts);
        }
    }

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
                        LOG.info("Registering script from file " + loc + ".");
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
