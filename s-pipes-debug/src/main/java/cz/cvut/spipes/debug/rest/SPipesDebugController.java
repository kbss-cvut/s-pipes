package cz.cvut.spipes.debug.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.spipes.debug.mapper.DtoMapper;
import cz.cvut.spipes.debug.model.ModuleExecution;
import cz.cvut.spipes.debug.model.PipelineExecution;
import cz.cvut.spipes.debug.service.DebugService;
import cz.cvut.spipes.debug.service.RelatedResourceService;
import cz.cvut.spipes.debug.service.ScriptService;

@EnableWebMvc
@RestController
public class SPipesDebugController {

    private final DebugService debugService;

    private final ScriptService scriptService;
    private final DtoMapper mapper;

    private final RelatedResourceService relatedResourceService;

    @Autowired
    public SPipesDebugController(DebugService debugService, ScriptService scriptService, DtoMapper dtoMapper, RelatedResourceService relatedResourceService) {
        this.debugService = debugService;
        this.scriptService = scriptService;
        this.mapper = dtoMapper;
        this.relatedResourceService = relatedResourceService;
    }

    @GetMapping(value = "/executions", produces = {
            JsonLd.MEDIA_TYPE,
            MediaType.APPLICATION_JSON_VALUE,
            MediaTypes.HAL_JSON_VALUE
    })
    public List<PipelineExecution> getAllExecutions() {
        return debugService.getAllPipelineExecutions();
    }

    @GetMapping(value = "/executions/{executionId}/modules", produces = {JsonLd.MEDIA_TYPE, MediaType.APPLICATION_JSON_VALUE})
    public List<ModuleExecution> getAllModulesByExecutionId(@PathVariable String executionId, @RequestParam(required = false) String orderBy) {
        return debugService.getAllModulesForExecution(executionId, orderBy);
    }

    @GetMapping(value = "/executions/{executionId}", produces = {JsonLd.MEDIA_TYPE, MediaType.APPLICATION_JSON_VALUE})
    public PipelineExecution getPipelineExecution(@PathVariable String executionId) {
        return debugService.getPipelineExecutionById(executionId);
    }

    @GetMapping(value = "/triple-origin/{executionId}")
    public List<ModuleExecution> test(@PathVariable String executionId, @RequestParam(required = false) String pattern) {
        List<ModuleExecution> executions = scriptService.findTripleOrigin(executionId, pattern);
        return null;
    }
}

