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
import cz.cvut.spipes.debug.dto.ModuleExecutionDto;
import cz.cvut.spipes.debug.dto.PipelineComparisonResultDto;
import cz.cvut.spipes.debug.dto.PipelineExecutionDto;
import cz.cvut.spipes.debug.service.DebugService;
import cz.cvut.spipes.debug.service.ScriptService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

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
    public List<PipelineExecutionDto> getAllExecutions() {
        return debugService.getAllPipelineExecutions();
    }

    @GetMapping(value = "/executions/{executionId}/modules", produces = {JsonLd.MEDIA_TYPE, MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(value = "Get all modules in execution", response = List.class)
    public List<ModuleExecutionDto> getAllModulesByExecutionIdWithExecutionTime(
            @ApiParam(
                    name = "executionId",
                    type = "String",
                    value = "Pipeline execution identifier",
                    example = "1682959945146003",
                    required = true)
            @PathVariable String executionId,
            @ApiParam(
                    name = "orderBy",
                    type = "String",
                    value = "Specifies orderBy field in ModuleExecutionDto",
                    example = "duration")
            @RequestParam(required = false) String orderBy,
            @ApiParam(
                    name = "orderType",
                    type = "String",
                    value = "ASC or DESC",
                    example = "ASC")
            @RequestParam(required = false) String orderType) {
        return debugService.getAllModuleExecutionsSorted(executionId, orderBy, orderType);
    }

    @GetMapping(value = "/executions/{executionId}", produces = {JsonLd.MEDIA_TYPE, MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(value = "Get pipeline execution", response = PipelineExecutionDto.class)
    public PipelineExecutionDto getPipelineExecution(
            @ApiParam(
                    name = "executionId",
                    type = "String",
                    value = "Pipeline execution identifier",
                    example = "1682959945146003",
                    required = true)
            @PathVariable String executionId) {
        return debugService.getPipelineExecutionById(executionId);
    }


    @GetMapping(value = "/executions/{executionId}/compare/{executionToCompareId}", produces = {JsonLd.MEDIA_TYPE, MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(value = "Compare pipeline executions", response = PipelineComparisonResultDto.class)
    public PipelineComparisonResultDto compareExecutions(
            @ApiParam(
                    name = "executionId",
                    type = "String",
                    value = "Pipeline execution identifier",
                    example = "1682959945146003",
                    required = true)
            @PathVariable String executionId,
            @ApiParam(
                    name = "executionToCompareId",
                    type = "String",
                    value = "Pipeline execution identifier",
                    example = "1682959945146003",
                    required = true)
            @PathVariable String executionToCompareId) {
        return debugService.compareExecutions(executionId, executionToCompareId);
    }

    @GetMapping(value = "/triple-origin/{executionId}")
    @ApiOperation(value = "Find triple origin", response = List.class, notes = TRIPLE_FORMAT_NOTE)
    public List<ModuleExecutionDto> findTripleOrigin(
            @ApiParam(
                    name = "executionId",
                    type = "String",
                    value = "Pipeline execution identifier",
                    example = "1682959945146003",
                    required = true)
            @PathVariable String executionId,
            @ApiParam(
                    name = "graphPattern",
                    type = "String",
                    value = "Triple pattern that is wanted to be found",
                    example = "<http://some/subject> <http://some/predicate> <http://some/object>",
                    required = true)
            @RequestParam String graphPattern) {
        return scriptService.findTripleOrigin(executionId, graphPattern);
    }

    @GetMapping(value = "/triple-elimination/{executionId}")
    @ApiOperation(value = "Find where triple was removed", response = List.class, notes = TRIPLE_FORMAT_NOTE)
    public List<ModuleExecutionDto> findTripleElimination(
            @ApiParam(
                    name = "executionId",
                    type = "String",
                    value = "Pipeline execution identifier",
                    example = "1682959945146003",
                    required = true)
            @PathVariable String executionId,
            @ApiParam(
                    name = "triple pattern",
                    type = "String",
                    value = "Triple pattern that is wanted to be found",
                    example = "<http://some/subject> <http://some/predicate> \"some string!\"",
                    required = true)
            @RequestParam String graphPattern) {
        return scriptService.findTripleEliminationOrigin(executionId, graphPattern);
    }

    @GetMapping(value = "/variable-origin/{executionId}")
    @ApiOperation(value = "Find where variable was created", response = List.class)
    public List<ModuleExecutionDto> findVariableOrigin(
            @ApiParam(
                    name = "executionId",
                    type = "String",
                    value = "Pipeline execution identifier",
                    example = "1682959945146003",
                    required = true)
            @PathVariable String executionId,
            @ApiParam(
                    name = "variable",
                    type = "String",
                    value = "Name of variable",
                    example = "firstName",
                    required = true)
            @RequestParam String variable) {
        return scriptService.findVariableOrigin(executionId, variable);
    }
}

