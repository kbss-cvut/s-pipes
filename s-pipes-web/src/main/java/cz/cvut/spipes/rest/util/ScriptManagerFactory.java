package cz.cvut.spipes.rest.util;

import cz.cvut.spipes.manager.OntoDocManager;
import cz.cvut.spipes.manager.OntologyDocumentManager;
import cz.cvut.spipes.manager.SPipesScriptManager;

import java.util.List;

public class ScriptManagerFactory {

    private static SPipesScriptManager scriptManager = null;

    public static synchronized SPipesScriptManager getSingletonSPipesScriptManager() {

        if (scriptManager == null) {
            OntologyDocumentManager ontoDocManager = OntoDocManager.getInstance();
            List<String> globalScripts = ContextLoaderHelper.registerGlobalScripts(ontoDocManager, false);
            scriptManager = new SPipesScriptManager(ontoDocManager, globalScripts);
        }
        return scriptManager;
    }
}
