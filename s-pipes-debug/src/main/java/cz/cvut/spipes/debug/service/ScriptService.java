package cz.cvut.spipes.debug.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import cz.cvut.spipes.debug.model.ModuleExecution;
import cz.cvut.spipes.debug.persistance.dao.TransformationDao;
import cz.cvut.spipes.debug.tree.ExecutionTree;
import cz.cvut.spipes.manager.SPipesScriptManager;
import cz.cvut.spipes.modules.Module;
import cz.cvut.spipes.util.ScriptManagerFactory;


@Service
public class ScriptService {
    private final SPipesScriptManager scriptManager;

    private final TransformationDao transformationDao;

    private final DebugService debugService;

    public ScriptService(TransformationDao transformationDao, DebugService debugService) {
        this.transformationDao = transformationDao;
        this.debugService = debugService;
        scriptManager = ScriptManagerFactory.getSingletonSPipesScriptManager();
    }

    public List<ModuleExecution> findTripleOrigin(String executionId, String subjectPattern, String predicatePattern, String objectPattern) {
        List<String> context = transformationDao.findModuleOutputContextsForTriple(executionId, subjectPattern, predicatePattern, objectPattern);
        List<String> moduleExecutionIris = context.stream()
                .map(i -> i.replace("/output", ""))
                .collect(Collectors.toList());
        List<ModuleExecution> moduleExecutions = debugService.getAllModulesForExecution(executionId, null);
        ExecutionTree executionTree = new ExecutionTree(moduleExecutions);
        return executionTree.findEarliest(moduleExecutionIris);
    }
}