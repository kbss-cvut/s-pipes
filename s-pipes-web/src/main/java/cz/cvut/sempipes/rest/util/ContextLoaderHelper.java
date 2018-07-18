package cz.cvut.sempipes.rest.util;

import cz.cvut.sempipes.config.ContextLoaderConfig;
import cz.cvut.sempipes.manager.OntoDocManager;
import cz.cvut.sempipes.manager.OntologyDocumentManager;
import cz.cvut.sempipes.manager.SempipesScriptManager;
import cz.cvut.sempipes.util.CoreConfigProperies;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import org.apache.jena.util.LocationMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Miroslav Blasko on 13.1.17.
 */
public class ContextLoaderHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ContextLoaderHelper.class);

    // TODO should not point to scriptManager
    public static void updateContextsIfNecessary(SempipesScriptManager scriptManager) {
        if (isKeepUpdated()) {
            LOG.warn("Updating contexts which is not thread safe -- don't use in in production environment.");
            OntologyDocumentManager ontoDocManager = OntoDocManager.getInstance();
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
