package cz.cvut.sempipes.service;

import cz.cvut.sempipes.eccairs.ConfigProperies;
import cz.cvut.sempipes.manager.OntoDocManager;
import cz.cvut.sempipes.manager.OntologyDocumentManager;
import cz.cvut.sempipes.manager.SempipesScriptManager;
import cz.cvut.sempipes.registry.StreamResourceRegistry;
import cz.cvut.sempipes.util.RDFMimeType;
import cz.cvut.sempipes.util.RestUtils;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by Miroslav Blasko on 19.12.16.
 */
@RestController
@EnableWebMvc
public class SempipesContextController {


    private static final Logger LOG = LoggerFactory.getLogger(SempipesServiceController.class);
    private SempipesScriptManager scriptManager;
    private List<String> globalScripts;
    private OntologyDocumentManager ontoDocManager;

    public SempipesContextController() {
        // TODO use spring injection instead
        List<Path> scriptDirs  = Arrays
                .stream(ConfigProperies.get("scriptDirs").split(";"))
                .map(path -> Paths.get(path))
                .collect(Collectors.toList());

        ontoDocManager = OntoDocManager.getInstance();
        globalScripts = SempipesScriptManager.getGlobalScripts(ontoDocManager, scriptDirs);
        OntoDocManager.registerAllSPINModules();

        scriptManager = new SempipesScriptManager(ontoDocManager, globalScripts);

    }

    @RequestMapping(
            value = "/contexts/{id}/data",
            method = RequestMethod.GET,
            produces = RDFMimeType.TURTLE_STRING
    ) // TODO returns only sample script
    public Model registerStreamResource(@PathVariable("id") String id) {

        String scriptUri = globalScripts.stream()
                .filter(uri -> uri.contains("fss") || uri.contains("eccairs")).findFirst()
                .orElseGet(() -> {return globalScripts.get(0);});
        String modulesUri = globalScripts.stream()
                .filter(uri -> uri.endsWith("module")).findFirst()
                .orElseGet(() -> {return globalScripts.get(0);});

        //get(0);
        Model model = ontoDocManager.getOntology(scriptUri);
        model.add(ontoDocManager.getOntology(modulesUri));

        return model;
    }
}
