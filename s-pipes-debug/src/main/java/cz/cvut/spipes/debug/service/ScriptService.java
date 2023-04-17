package cz.cvut.spipes.debug.service;

import static cz.cvut.spipes.debug.util.IdUtils.getTransformationIriFromId;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import cz.cvut.spipes.debug.exception.NotFoundException;
import cz.cvut.spipes.debug.mapper.TransformationDtoMapper;
import cz.cvut.spipes.debug.model.ModuleExecution;
import cz.cvut.spipes.debug.persistance.dao.InputBindingDao;
import cz.cvut.spipes.debug.persistance.dao.TransformationDao;
import cz.cvut.spipes.debug.tree.ExecutionTree;
import cz.cvut.spipes.model.Thing;
import cz.cvut.spipes.model.Transformation;


@Service
public class ScriptService {

    private final TransformationDao transformationDao;

    private final InputBindingDao inputBindingDao;

    private final TransformationDtoMapper mapper;

    private static final String NO_MATCHING_MODULES = "No matching modules found for ";

    private static final String NOT_FOUND_ERROR_PATTERN = NO_MATCHING_MODULES + "pattern %s";

    private static final String NOT_FOUND_ERROR_VARIABLE = NO_MATCHING_MODULES + "variable %s";

    public ScriptService(TransformationDao transformationDao, InputBindingDao inputBindingDao, TransformationDtoMapper mapper) {
        this.transformationDao = transformationDao;
        this.inputBindingDao = inputBindingDao;
        this.mapper = mapper;
    }

    public List<ModuleExecution> findTripleOrigin(String executionId, String graphPattern) {
        Predicate<ModuleExecution> predicate = moduleExecution -> transformationDao.askContainOutput(moduleExecution.getHas_rdf4j_output().getId(), graphPattern);
        return findFirstModule(executionId, predicate, graphPattern);
    }

    public List<ModuleExecution> findTripleEliminationOrigin(String executionId, String graphPattern) {
        Predicate<ModuleExecution> predicate = moduleExecution -> transformationDao.askContainInputAndNotContainOutput(moduleExecution.getHas_rdf4j_input().getId(),
                moduleExecution.getHas_rdf4j_output().getId(), graphPattern);
        return findFirstModule(executionId, predicate, graphPattern);
    }

    public List<ModuleExecution> findFirstModule(String executionId, Predicate<ModuleExecution> predicate, String pattern) {
        List<ModuleExecution> moduleExecutions = getModuleExecutions(executionId);
        List<ModuleExecution> modulesWithMatchingPattern = new ArrayList<>();
        for (ModuleExecution moduleExecution : moduleExecutions) {
            if (predicate.test(moduleExecution)) {
                modulesWithMatchingPattern.add(moduleExecution);
            }
        }
        if (modulesWithMatchingPattern.isEmpty()) {
            throw new NotFoundException(String.format(NOT_FOUND_ERROR_PATTERN, pattern));
        }
        ExecutionTree executionTree = new ExecutionTree(moduleExecutions);
        return executionTree.findEarliest(modulesWithMatchingPattern);
    }

    public List<ModuleExecution> findVariableOrigin(String executionId, String variable) {
        List<ModuleExecution> moduleExecutions = getModuleExecutions(executionId);
        List<ModuleExecution> modulesWithBoundVariable = new ArrayList<>();
        moduleExecutions.forEach(m -> {
            Thing inputBinding = m.getHas_input_binding();
            if (inputBinding != null) {
                if (inputBindingDao.askHasBoundVariable(m.getHas_input_binding().getId(), variable)) {
                    modulesWithBoundVariable.add(m);
                }
            }
        });
        if (modulesWithBoundVariable.isEmpty()) {
            throw new NotFoundException(String.format(NOT_FOUND_ERROR_VARIABLE, variable));
        }
        ExecutionTree executionTree = new ExecutionTree(moduleExecutions);
        return executionTree.findEarliest(modulesWithBoundVariable);
    }

    private List<ModuleExecution> getModuleExecutions(String executionId) {
        String pipelineExecutionIri = getTransformationIriFromId(executionId);
        Transformation transformation = transformationDao.findByUri(pipelineExecutionIri);

        return transformation.getHas_part()
                .stream().map(mapper::transformationToModuleExecution).collect(Collectors.toList());
    }
}