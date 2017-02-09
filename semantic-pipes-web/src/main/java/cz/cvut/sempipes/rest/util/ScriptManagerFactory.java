package cz.cvut.sempipes.rest.util;

import cz.cvut.sempipes.manager.OntoDocManager;
import cz.cvut.sempipes.manager.OntologyDocumentManager;
import cz.cvut.sempipes.manager.SempipesScriptManager;

import java.util.List;

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
