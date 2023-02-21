package cz.cvut.spipes.debug.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.catalina.Pipeline;
import org.springframework.stereotype.Service;

import cz.cvut.spipes.debug.exception.NotFoundException;
import cz.cvut.spipes.debug.mapper.ModuleExecutionMapper;
import cz.cvut.spipes.debug.dto.ModuleExecutionDto;
import cz.cvut.spipes.debug.persistance.dao.InputBindingDao;
import cz.cvut.spipes.debug.persistance.dao.ModuleExecutionDao;
import cz.cvut.spipes.debug.persistance.dao.PipelineExecutionDao;
import cz.cvut.spipes.debug.tree.ExecutionTree;
import cz.cvut.spipes.model.ModuleExecution;
import cz.cvut.spipes.model.PipelineExecution;
import cz.cvut.spipes.model.Thing;


@Service
public class ScriptService {

    private final ModuleExecutionDao moduleExecutionDao;
    private final PipelineExecutionDao pipelineExecutionDao;
    private final InputBindingDao inputBindingDao;

    private final ModuleExecutionMapper moduleExecutionMapper;

    private static final String NO_MATCHING_MODULES = "No matching modules found for ";

    private static final String NOT_FOUND_ERROR_PATTERN = NO_MATCHING_MODULES + "pattern %s";

    private static final String NOT_FOUND_ERROR_VARIABLE = NO_MATCHING_MODULES + "variable %s";

    public ScriptService(
            ModuleExecutionDao moduleExecutionDao,
            PipelineExecutionDao pipelineExecutionDao,
            InputBindingDao inputBindingDao,
            ModuleExecutionMapper moduleExecutionMapper) {
        this.moduleExecutionDao = moduleExecutionDao;
        this.pipelineExecutionDao = pipelineExecutionDao;
        this.inputBindingDao = inputBindingDao;
        this.moduleExecutionMapper = moduleExecutionMapper;
    }

    public List<ModuleExecutionDto> findTripleOrigin(String executionId, String graphPattern) {
        Predicate<ModuleExecution> predicate = moduleExecution -> moduleExecutionDao.askContainOutput(moduleExecution.getHas_rdf4j_output().getId(), graphPattern);
        return findFirstModule(executionId, predicate, graphPattern)
                .stream()
                .map(moduleExecutionMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ModuleExecutionDto> findTripleEliminationOrigin(String executionId, String graphPattern) {
        Predicate<ModuleExecution> predicate = moduleExecution -> moduleExecutionDao.askContainInputAndNotContainOutput(moduleExecution.getHas_rdf4j_input().getId(),
                moduleExecution.getHas_rdf4j_output().getId(), graphPattern);
        return findFirstModule(executionId, predicate, graphPattern).stream()
                .map(moduleExecutionMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ModuleExecution> findFirstModule(String executionId, Predicate<ModuleExecution> predicate, String pattern) {
        Set<ModuleExecution> moduleExecutions = getModulesForPipelineExecutionId(executionId);

        List<ModuleExecution> modulesWithMatchingPattern = new ArrayList<>();
        for (ModuleExecution ModuleExecution : moduleExecutions) {
            if (predicate.test(ModuleExecution)) {
                modulesWithMatchingPattern.add(ModuleExecution);
            }
        }
        if (modulesWithMatchingPattern.isEmpty()) {
            throw new NotFoundException(String.format(NOT_FOUND_ERROR_PATTERN, pattern));
        }
        ExecutionTree executionTree = new ExecutionTree(moduleExecutions);
        return executionTree.findEarliest(modulesWithMatchingPattern);
    }

    public List<ModuleExecutionDto> findVariableOrigin(String executionId, String variable) {
        Set<ModuleExecution> moduleExecutions = getModulesForPipelineExecutionId(executionId);

        List<ModuleExecution> modulesWithBoundVariable = new ArrayList<>();
        for (ModuleExecution m : moduleExecutions) {
            Set<Thing> inputBindings = m.getHas_input_binding();
            addModuleIfHasBoundVariable(m, inputBindings, modulesWithBoundVariable, variable);
        }
        if (modulesWithBoundVariable.isEmpty()) {
            throw new NotFoundException(String.format(NOT_FOUND_ERROR_VARIABLE, variable));
        }
        ExecutionTree executionTree = new ExecutionTree(moduleExecutions);
        return executionTree.findEarliest(modulesWithBoundVariable).stream()
                .map(moduleExecutionMapper::toDto)
                .collect(Collectors.toList());
    }

    private Set<ModuleExecution> getModulesForPipelineExecutionId(String pipelineExecutionId){
        PipelineExecution pipelineExecution = pipelineExecutionDao.findById(pipelineExecutionId);
        if(pipelineExecution == null){
            throw new NotFoundException("Pipeline execution with id " + pipelineExecutionId + " was not found");
        }
        return pipelineExecution.getHas_part();
    }

    private void addModuleIfHasBoundVariable(ModuleExecution ModuleExecution, Set<Thing> inputBindings, List<ModuleExecution> modulesWithBoundVariable, String variable) {
        for (Thing binding : inputBindings) {
            if (binding != null) {
                if (inputBindingDao.askHasBoundVariable(binding.getId(), variable)) {
                    modulesWithBoundVariable.add(ModuleExecution);
                    return;
                }
            }
        }
    }
}