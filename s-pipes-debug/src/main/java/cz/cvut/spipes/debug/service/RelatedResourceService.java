package cz.cvut.spipes.debug.service;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static cz.cvut.spipes.debug.util.DebugUtils.getExecutionIdFromIri;

import org.springframework.stereotype.Service;

import cz.cvut.spipes.debug.model.ModuleExecution;
import cz.cvut.spipes.debug.model.PipelineExecution;
import cz.cvut.spipes.debug.model.RelatedResource;
import cz.cvut.spipes.debug.rest.SPipesDebugController;

@Service
public class RelatedResourceService {

    public void addPipelineExecutionResources(PipelineExecution pipelineExecution) {
        //modules
        String linkToModules = linkTo(methodOn(SPipesDebugController.class)
                .getAllModulesByExecutionIdWithExecutionTime(getExecutionIdFromIri(pipelineExecution.getId()), null, null)).withRel("modules").getHref();
        RelatedResource relatedResourceModules = new RelatedResource();
        relatedResourceModules.setName("modules");
        relatedResourceModules.setLink(linkToModules);

        //pipeline execution
        String linkToPipelineExecution = linkTo(methodOn(SPipesDebugController.class)
                .getPipelineExecution(getExecutionIdFromIri(pipelineExecution.getId()))).withRel("pipeline execution").getHref();
        RelatedResource pipelineExecutionResource = new RelatedResource();
        pipelineExecutionResource.setName("pipeline");
        pipelineExecutionResource.setLink(linkToPipelineExecution);

        pipelineExecution.addRelated_resource(relatedResourceModules);
        pipelineExecution.addRelated_resource(pipelineExecutionResource);
    }

    public void addModuleExecutionResources(ModuleExecution moduleExecution) {
        //pipeline execution
        String linkToPipelineExecution = linkTo(methodOn(SPipesDebugController.class)
                .getPipelineExecution(getExecutionIdFromIri(moduleExecution.getExecuted_in())))
                .withRel("pipeline execution").getHref();
        RelatedResource pipelineExecutionResource = new RelatedResource();
        pipelineExecutionResource.setName("pipeline");
        pipelineExecutionResource.setLink(linkToPipelineExecution);

        moduleExecution.addRelated_resource(pipelineExecutionResource);
    }
}
