package cz.cvut.spipes.debug.service;

import static cz.cvut.spipes.debug.util.IdUtils.getTransformationIriFromId;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import cz.cvut.spipes.debug.mapper.TransformationDtoMapper;
import cz.cvut.spipes.debug.model.ModuleExecution;
import cz.cvut.spipes.debug.persistance.dao.TransformationDao;
import cz.cvut.spipes.debug.tree.ExecutionTree;
import cz.cvut.spipes.manager.SPipesScriptManager;
import cz.cvut.spipes.model.Transformation;
import cz.cvut.spipes.util.ScriptManagerFactory;


@Service
public class ScriptService {
    private final SPipesScriptManager scriptManager;

    private final TransformationDao transformationDao;

    private final DebugService debugService;

    private final TransformationDtoMapper mapper;

    public ScriptService(TransformationDao transformationDao, DebugService debugService, TransformationDtoMapper mapper) {
        this.transformationDao = transformationDao;
        this.debugService = debugService;
        this.mapper = mapper;
        scriptManager = ScriptManagerFactory.getSingletonSPipesScriptManager();
    }

    public List<ModuleExecution> findTripleOrigin(String executionId, String graphPattern) {
        Predicate<ModuleExecution> predicate = moduleExecution -> transformationDao.askContainOutput(moduleExecution.getHas_rdf4j_output().getId(), graphPattern);
        return findFirstModule(executionId, predicate);
    }

    public List<ModuleExecution> findTripleEliminationOrigin(String executionId, String graphPattern) {
        Predicate<ModuleExecution> predicate = moduleExecution -> transformationDao.askContainInputAndNotContainOutput(moduleExecution.getHas_rdf4j_input().getId(),
                moduleExecution.getHas_rdf4j_output().getId(), graphPattern);
        return findFirstModule(executionId, predicate);
    }

    public List<ModuleExecution> findFirstModule(String executionId, Predicate<ModuleExecution> predicate) {
        String pipelineExecutionIri = getTransformationIriFromId(executionId);
        Transformation transformation = transformationDao.findByUri(pipelineExecutionIri);

        List<ModuleExecution> moduleExecutions = transformation.getHas_part()
                .stream().map(mapper::transformationToModuleExecution).collect(Collectors.toList());
        List<ModuleExecution> modulesWithMatchingPattern = new ArrayList<>();
        for (ModuleExecution moduleExecution : moduleExecutions) {
            if (predicate.test(moduleExecution)) {
                modulesWithMatchingPattern.add(moduleExecution);
            }
        }
        ExecutionTree executionTree = new ExecutionTree(moduleExecutions);
        return executionTree.findEarliest(modulesWithMatchingPattern);
    }
}