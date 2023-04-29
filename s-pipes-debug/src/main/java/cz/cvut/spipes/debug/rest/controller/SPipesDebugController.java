package cz.cvut.spipes.debug.rest.controller;

import static cz.cvut.spipes.debug.config.SwaggerConstants.TRIPLE_FORMAT_NOTE;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.spipes.debug.dto.PipelineComparisonResultDto;
import cz.cvut.spipes.debug.model.ModuleExecution;
import cz.cvut.spipes.debug.model.PipelineExecution;
import cz.cvut.spipes.debug.service.DebugService;
import cz.cvut.spipes.debug.service.ScriptService;
import io.swagger.annotations.ApiOperation;

@RestController
public class SPipesDebugController {

    private final DebugService debugService;

    private final ScriptService scriptService;

    @Autowired
    public SPipesDebugController(DebugService debugService, ScriptService scriptService) {
        this.debugService = debugService;
        this.scriptService = scriptService;
    }

    @ApiOperation(value = "Get all executions", response = List.class)
    @GetMapping(value = "/executions", produces = {
            JsonLd.MEDIA_TYPE,
            MediaType.APPLICATION_JSON_VALUE,
            MediaTypes.HAL_JSON_VALUE
    })
    public List<PipelineExecution> getAllExecutions() {
        return debugService.getAllPipelineExecutions();
    }

    @GetMapping(value = "/executions/{executionId}/modules", produces = {JsonLd.MEDIA_TYPE, MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(value = "Get all modules in execution", response = List.class)
    public List<ModuleExecution> getAllModulesByExecutionIdWithExecutionTime(
            @PathVariable String executionId, @RequestParam(required = false) String orderBy,
            @RequestParam(required = false) String orderType) {
        return debugService.getAllModuleExecutionsSorted(executionId, orderBy, orderType);
    }

    @GetMapping(value = "/executions/{executionId}", produces = {JsonLd.MEDIA_TYPE, MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(value = "Get pipeline execution", response = PipelineExecution.class)
    public PipelineExecution getPipelineExecution(@PathVariable String executionId) {
        return debugService.getPipelineExecutionById(executionId);
    }


    @GetMapping(value = "/executions/{executionId}/compare/{executionToCompareId}", produces = {JsonLd.MEDIA_TYPE, MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(value = "Compare pipeline executions", response = PipelineComparisonResultDto.class)
    public PipelineComparisonResultDto compareExecutions(@PathVariable String executionId, @PathVariable String executionToCompareId) {
        return debugService.compareExecutions(executionId, executionToCompareId);
    }

    @GetMapping(value = "/triple-origin/{executionId}")
    @ApiOperation(value = "Find triple origin", response = List.class, notes = TRIPLE_FORMAT_NOTE)
    public List<ModuleExecution> findTripleOrigin(
            @PathVariable String executionId,
            @RequestParam String graphPattern) {
        return scriptService.findTripleOrigin(executionId, graphPattern);
    }

    @GetMapping(value = "/triple-elimination/{executionId}")
    @ApiOperation(value = "Find where triple was removed", response = List.class, notes = TRIPLE_FORMAT_NOTE)
    public List<ModuleExecution> findTripleElimination(
            @PathVariable String executionId,
            @RequestParam String graphPattern) {
        return scriptService.findTripleEliminationOrigin(executionId, graphPattern);
    }

    @GetMapping(value = "/variable-origin/{executionId}")
    @ApiOperation(value = "Find where variable was created", response = List.class)
    public List<ModuleExecution> findVariableOrigin(
            @PathVariable String executionId,
            @RequestParam String variable) {
        return scriptService.findVariableOrigin(executionId, variable);
    }
}

