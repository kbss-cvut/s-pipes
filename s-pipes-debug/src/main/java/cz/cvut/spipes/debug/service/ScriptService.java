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
import cz.cvut.spipes.debug.persistance.dao.TransformationDao;
import cz.cvut.spipes.debug.tree.ExecutionTree;
import cz.cvut.spipes.model.Transformation;


@Service
public class ScriptService {

    private final TransformationDao transformationDao;

    private final TransformationDtoMapper mapper;

    private static final String NOT_FOUND_ERROR = "No matching modules found for pattern %s";

    public ScriptService(TransformationDao transformationDao, TransformationDtoMapper mapper) {
        this.transformationDao = transformationDao;
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
        if (modulesWithMatchingPattern.isEmpty()) {
            throw new NotFoundException(String.format(NOT_FOUND_ERROR, pattern));
        }
        ExecutionTree executionTree = new ExecutionTree(moduleExecutions);
        return executionTree.findEarliest(modulesWithMatchingPattern);
    }
}