package cz.cvut.sempipes.service;

import cz.cvut.sempipes.constants.SM;
import cz.cvut.sempipes.engine.PipelineFactory;
import cz.cvut.sempipes.manager.OntologyDocumentManager;
import cz.cvut.sempipes.manager.SempipesScriptManager;
import cz.cvut.sempipes.util.JenaUtils;
import cz.cvut.sempipes.util.RDFMimeType;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.LinkedList;
import java.util.List;
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
        scriptManager = ScriptManagerFactory.getSingletonSPipesScriptManager();
        ontoDocManager = scriptManager.getOntoDocManager();
        globalScripts = new LinkedList<>(scriptManager.getGlobalScripts());
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
        Model inModel = ontoDocManager.getOntology(scriptUri);
        inModel.add(ontoDocManager.getOntology(modulesUri));

        Model outModel = ModelFactory.createDefaultModel();
        outModel.add(inModel);
        outModel.add(SempipesContextController.createInferences(inModel));

        return outModel;
    }


     static List<Statement> createInferences(Model model) {

        return PipelineFactory.getModuleTypes().keySet().stream()
                .flatMap(mt -> model.listSubjectsWithProperty(RDF.type, mt).toSet().stream())
                .map(m -> model.createStatement(
                        m, RDF.type, SM.Modules
                )).collect(Collectors.toList());
    }
}
