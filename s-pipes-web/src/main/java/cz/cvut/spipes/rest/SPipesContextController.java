package cz.cvut.spipes.rest;

import cz.cvut.spipes.constants.SM;
import cz.cvut.spipes.engine.PipelineFactory;
import cz.cvut.spipes.manager.OntologyDocumentManager;
import cz.cvut.spipes.manager.SPipesScriptManager;
import cz.cvut.spipes.rest.util.ContextLoaderHelper;
import cz.cvut.spipes.rest.util.ScriptManagerFactory;
import cz.cvut.spipes.util.RDFMimeType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@EnableWebMvc
public class SPipesContextController {

    private final SPipesScriptManager scriptManager;
    private final List<String> globalScripts;
    private final OntologyDocumentManager ontoDocManager;

    public SPipesContextController() {
        scriptManager = ScriptManagerFactory.getSingletonSPipesScriptManager();
        ontoDocManager = scriptManager.getOntoDocManager();
        globalScripts = new LinkedList<>(scriptManager.getGlobalScripts());
    }

    @Operation(
        summary = "Retrieve context data",
        responses = {
            @ApiResponse(responseCode = "200", description = "RDF data",
                content = @Content(
                    mediaType = RDFMimeType.TURTLE_STRING,
                    schema = @Schema(hidden = true)
                )
            )
        }
    )
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

        outModel.add(SM.JENA.next, RDF.type, OWL.ObjectProperty);

        return outModel;
    }


     static List<Statement> createInferences(Model model) {

        return PipelineFactory.getModuleTypes().keySet().stream()
                .flatMap(mt -> model.listSubjectsWithProperty(RDF.type, mt).toSet().stream())
                .map(m -> model.createStatement(
                        m, RDF.type, SM.JENA.Modules
                )).collect(Collectors.toList());
    }
}
