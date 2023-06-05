package cz.cvut.spipes.rest;

import cz.cvut.spipes.constants.SM;
import cz.cvut.spipes.engine.PipelineFactory;
import cz.cvut.spipes.manager.OntologyDocumentManager;
import cz.cvut.spipes.manager.SPipesScriptManager;
import cz.cvut.spipes.util.ContextLoaderHelper;
import cz.cvut.spipes.util.RDFMimeType;
import cz.cvut.spipes.util.ScriptManagerFactory;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@EnableWebMvc
public class SPipesContextController {


    private static final Logger LOG = LoggerFactory.getLogger(SPipesServiceController.class);
    private SPipesScriptManager scriptManager;
    private List<String> globalScripts;
    private OntologyDocumentManager ontoDocManager;

    public SPipesContextController() {
        scriptManager = ScriptManagerFactory.getSingletonSPipesScriptManager();
        ontoDocManager = scriptManager.getOntoDocManager();
        globalScripts = new LinkedList<>(scriptManager.getGlobalScripts());
    }

    @RequestMapping(
            value = "/contexts/{id}/data",
            method = RequestMethod.GET,
            produces = RDFMimeType.TURTLE_STRING
    ) // TODO returns only sample script
    public Model retrieveContextData(@PathVariable("id") String id) {

        ContextLoaderHelper.updateContextsIfNecessary(scriptManager);

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
        outModel.add(SPipesContextController.createInferences(inModel));

        outModel.add(SM.next, RDF.type, OWL.ObjectProperty);

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
