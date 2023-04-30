package cz.cvut.spipes.debug.service;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static cz.cvut.spipes.debug.util.IdUtils.extractPipelineExecutionId;
import static cz.cvut.spipes.debug.util.IdUtils.getExecutionIdFromIri;

import java.util.List;

import org.springframework.stereotype.Service;

import cz.cvut.spipes.debug.dto.ModuleExecutionDto;
import cz.cvut.spipes.debug.dto.PipelineExecutionDto;
import cz.cvut.spipes.debug.dto.RelatedResource;
import cz.cvut.spipes.debug.rest.controller.SPipesDebugController;

@Service
public class RelatedResourceService {

    public void addPipelineExecutionResources(PipelineExecutionDto pipelineExecutionDto) {
        String moduleExecutionName = "Module execution";
        String linkToModules = linkTo(methodOn(SPipesDebugController.class)
                .getAllModulesByExecutionIdWithExecutionTime(getExecutionIdFromIri(pipelineExecutionDto.getId()), "ORDER_BY", "ORDER_TYPE")).withRel(moduleExecutionName).getHref();
        RelatedResource relatedResourceModules = createResource(moduleExecutionName, linkToModules);
        relatedResourceModules.addParam("ORDER_BY", List.of("duration", "output-triples", "input-triples", "start-time"));
        relatedResourceModules.addParam("ORDER_TYPE", List.of("ASC", "DESC"));

        String pipelineExecutionName = "Pipeline execution";
        String linkToPipelineExecution = linkTo(methodOn(SPipesDebugController.class)
                .getPipelineExecution(getExecutionIdFromIri(pipelineExecutionDto.getId()))).withRel(pipelineExecutionName).getHref();
        RelatedResource pipelineExecutionResource = createResource(pipelineExecutionName,linkToPipelineExecution);

        String comparePipelinesName = "Compare pipelines";
        String comparePipelinesLink = linkTo(methodOn(SPipesDebugController.class)
                .compareExecutions(getExecutionIdFromIri(pipelineExecutionDto.getId()), "otherPipelineExecutionId"))
                .withRel(comparePipelinesName).getHref();
        RelatedResource comparePipelinesResource = createResource(comparePipelinesName, comparePipelinesLink);

        String tripleOriginName = "Find triple origin";
        String tripleOriginLink = linkTo(methodOn(SPipesDebugController.class)
                .findTripleOrigin(getExecutionIdFromIri(pipelineExecutionDto.getId()), "your-graph-pattern"))
                .withRel(tripleOriginName).getHref();
        RelatedResource tripleOriginResource = createResource(tripleOriginName, tripleOriginLink);

        String tripleEliminationName = "Find triple elimination";
        String tripleEliminationLink = linkTo(methodOn(SPipesDebugController.class)
                .findTripleElimination(getExecutionIdFromIri(pipelineExecutionDto.getId()), "your-graph-pattern"))
                .withRel(tripleEliminationName).getHref();
        RelatedResource tripleEliminationResource = createResource(tripleEliminationName, tripleEliminationLink);

        String variableOriginName = "Find variable origin";
        String variableOriginLink = linkTo(methodOn(SPipesDebugController.class)
                .findVariableOrigin(getExecutionIdFromIri(pipelineExecutionDto.getId()), "variable-name"))
                .withRel(variableOriginName).getHref();
        RelatedResource variableOriginResource = createResource(variableOriginName, variableOriginLink);

        pipelineExecutionDto.addRelated_resource(relatedResourceModules);
        pipelineExecutionDto.addRelated_resource(pipelineExecutionResource);
        pipelineExecutionDto.addRelated_resource(comparePipelinesResource);
        pipelineExecutionDto.addRelated_resource(tripleOriginResource);
        pipelineExecutionDto.addRelated_resource(tripleEliminationResource);
        pipelineExecutionDto.addRelated_resource(variableOriginResource);
    }

    public void addModuleExecutionResources(ModuleExecutionDto moduleExecutionDto) {
        String pipelineExecutionName = "Pipeline execution";
        String linkToPipelineExecution = linkTo(methodOn(SPipesDebugController.class)
                .getPipelineExecution(getExecutionIdFromIri(moduleExecutionDto.getExecuted_in().getId()))).withRel(pipelineExecutionName).getHref();
        RelatedResource pipelineExecutionResource = new RelatedResource();
        pipelineExecutionResource.setName(pipelineExecutionName);
        pipelineExecutionResource.setLink(linkToPipelineExecution);

        moduleExecutionDto.addRelated_resource(pipelineExecutionResource);
    }

    private RelatedResource createResource(String name, String link) {
        RelatedResource resource = new RelatedResource();
        resource.setName(name);
        resource.setLink(link);
        return resource;
    }
}
