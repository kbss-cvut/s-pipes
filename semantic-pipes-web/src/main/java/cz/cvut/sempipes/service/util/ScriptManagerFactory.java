package cz.cvut.sempipes.service.util;

import cz.cvut.sempipes.manager.OntoDocManager;
import cz.cvut.sempipes.manager.OntologyDocumentManager;
import cz.cvut.sempipes.manager.SempipesScriptManager;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Miroslav Blasko on 12.1.17.
 */
public class ScriptManagerFactory {

    private static SempipesScriptManager scriptManager = null;

    public static synchronized SempipesScriptManager getSingletonSPipesScriptManager() {

        if (scriptManager == null) {
            OntologyDocumentManager ontoDocManager = OntoDocManager.getInstance();
            List<String> globalScripts = ContextLoaderHelper.registerGlobalScripts(ontoDocManager);
            scriptManager = new SempipesScriptManager(ontoDocManager, globalScripts);
        }
        return scriptManager;
    }
}
