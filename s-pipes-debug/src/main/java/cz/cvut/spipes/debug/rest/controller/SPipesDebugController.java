package cz.cvut.spipes.debug.rest.controller;

import static cz.cvut.spipes.debug.config.SwaggerConstants.TRIPLE_FORMAT_NOTE;

import java.util.List;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
public class SPipesDebugController {

    private final DebugService debugService;

    private final ScriptService scriptService;

    @Autowired
    public SPipesDebugController(DebugService debugService, ScriptService scriptService) {
        this.debugService = debugService;
        this.scriptService = scriptService;
    }

    @Operation(summary = "Get all pipeline executions", description = "Retrieve all pipeline executions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of pipeline executions")
    })
    @GetMapping(value = "/executions", produces = {
            JsonLd.MEDIA_TYPE,
            MediaType.APPLICATION_JSON_VALUE,
            MediaTypes.HAL_JSON_VALUE
    })
    public List<PipelineExecutionDto> getAllExecutions() {
        return debugService.getAllPipelineExecutions();
    }

    @Operation(summary = "Get all module executions in pipeline execution", description = "Retrieve module executions for a given pipeline execution ID")
    @GetMapping(value = "/executions/{executionId}/modules", produces = {JsonLd.MEDIA_TYPE, MediaType.APPLICATION_JSON_VALUE})
    public List<ModuleExecutionDto> getAllModulesByExecutionIdWithExecutionTime(
            @Parameter(
                    name = "executionId",
                    description = "Pipeline execution identifier",
                    example = "1682959945146003",
                    required = true)
            @PathVariable String executionId,
            @Parameter(
                    name = "orderBy",
                    description = "Specifies orderBy field in ModuleExecutionDto",
                    example = "duration")
            @RequestParam(required = false) String orderBy,
            @Parameter(
                    name = "orderType",
                    description = "ASC or DESC",
                    example = "ASC")
            @RequestParam(required = false) String orderType) {
        return debugService.getAllModuleExecutionsSorted(executionId, orderBy, orderType);
    }

    @Operation(summary = "Get pipeline execution", description = "Retrieve a specific pipeline execution by ID")
    @GetMapping(value = "/executions/{executionId}", produces = {JsonLd.MEDIA_TYPE, MediaType.APPLICATION_JSON_VALUE})
    public PipelineExecutionDto getPipelineExecution(
            @Parameter(
                    name = "executionId",
                    description = "Pipeline execution identifier",
                    example = "1682959945146003",
                    required = true)
            @PathVariable String executionId) {
        return debugService.getPipelineExecutionById(executionId);
    }

    @Operation(summary = "Compare pipeline executions", description = "Compare two pipeline executions and find the first difference")
    @GetMapping(value = "/executions/{executionId}/compare/{executionToCompareId}", produces = {JsonLd.MEDIA_TYPE, MediaType.APPLICATION_JSON_VALUE})
    public PipelineComparisonResultDto compareExecutions(
            @Parameter(
                    name = "executionId",
                    description = "Pipeline execution identifier",
                    example = "1682959945146003",
                    required = true)
            @PathVariable String executionId,
            @Parameter(
                    name = "executionToCompareId",
                    description = "Pipeline execution identifier to compare against",
                    example = "1682959945146003",
                    required = true)
            @PathVariable String executionToCompareId) {
        return debugService.compareExecutions(executionId, executionToCompareId);
    }

    @Operation(summary = "Find module execution that created a triple", description = "Retrieve the module execution responsible for creating a specific triple")
    @GetMapping(value = "/triple-origin/{executionId}")
    public List<ModuleExecutionDto> findTripleOrigin(
            @Parameter(
                    name = "executionId",
                    description = "Pipeline execution identifier",
                    example = "1682959945146003",
                    required = true)
            @PathVariable String executionId,
            @Parameter(
                    name = "graphPattern",
                    description = "Triple pattern to search for",
                    example = "<http://some/subject> <http://some/predicate> <http://some/object>",
                    required = true)
            @RequestParam String graphPattern) {
        return scriptService.findTripleOrigin(executionId, graphPattern);
    }

    @Operation(summary = "Find module execution where a triple was removed", description = "Retrieve the module execution responsible for removing a specific triple")
    @GetMapping(value = "/triple-elimination/{executionId}")
    public List<ModuleExecutionDto> findTripleElimination(
            @Parameter(
                    name = "executionId",
                    description = "Pipeline execution identifier",
                    example = "1682959945146003",
                    required = true)
            @PathVariable String executionId,
            @Parameter(
                    name = "graphPattern",
                    description = "Triple pattern to search for",
                    example = "<http://some/subject> <http://some/predicate> \"some string!\"",
                    required = true)
            @RequestParam String graphPattern) {
        return scriptService.findTripleEliminationOrigin(executionId, graphPattern);
    }

    @Operation(summary = "Find module execution where a variable was created", description = "Retrieve the module execution responsible for creating a specific variable")
    @GetMapping(value = "/variable-origin/{executionId}")
    public List<ModuleExecutionDto> findVariableOrigin(
            @Parameter(
                    name = "executionId",
                    description = "Pipeline execution identifier",
                    example = "1682959945146003",
                    required = true)
            @PathVariable String executionId,
            @Parameter(
                    name = "variable",
                    description = "Name of the variable",
                    example = "firstName",
                    required = true)
            @RequestParam String variable) {
        return scriptService.findVariableOrigin(executionId, variable);
    }
}
