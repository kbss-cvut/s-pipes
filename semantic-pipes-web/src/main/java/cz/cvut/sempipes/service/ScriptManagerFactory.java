package cz.cvut.sempipes.service;

import cz.cvut.sempipes.eccairs.ConfigProperies;
import cz.cvut.sempipes.manager.OntoDocManager;
import cz.cvut.sempipes.manager.OntologyDocumentManager;
import cz.cvut.sempipes.manager.SempipesScriptManager;
import cz.cvut.sempipes.registry.JenaResourceRegistry;
import cz.cvut.sempipes.repository.SMScriptCollectionRepository;
import org.apache.jena.rdf.model.Resource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Miroslav Blasko on 12.1.17.
 */
public class ScriptManagerFactory {

    private static SempipesScriptManager scriptManager = getSingletonSPipesScriptManager();

    public static SempipesScriptManager getSingletonSPipesScriptManager() {

        if (scriptManager == null) {
            List<Path> scriptDirs  = Arrays
                    .stream(ConfigProperies.get("scriptDirs").split(";"))
                    .map(path -> Paths.get(path))
                    .collect(Collectors.toList());

            OntologyDocumentManager ontoDocManager = OntoDocManager.getInstance();
            List<String> globalScripts = SempipesScriptManager.getGlobalScripts(ontoDocManager, scriptDirs);
            OntoDocManager.registerAllSPINModules();

            scriptManager = new SempipesScriptManager(ontoDocManager, globalScripts);
        }
        return scriptManager;
    }

}
