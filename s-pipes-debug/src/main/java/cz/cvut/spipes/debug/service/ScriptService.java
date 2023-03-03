package cz.cvut.spipes.debug.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import cz.cvut.spipes.Vocabulary;
import cz.cvut.spipes.debug.model.ModuleExecution;
import cz.cvut.spipes.debug.persistance.dao.TransformationDao;
import cz.cvut.spipes.manager.SPipesScriptManager;
import cz.cvut.spipes.model.Transformation;
import cz.cvut.spipes.modules.Module;
import cz.cvut.spipes.util.ScriptManagerFactory;


@Service
public class ScriptService {
    private final SPipesScriptManager scriptManager;

    private final TransformationDao transformationDao;

    public ScriptService(TransformationDao transformationDao) {
        this.transformationDao = transformationDao;
        scriptManager = ScriptManagerFactory.getSingletonSPipesScriptManager();
    }

    public void a(String id) {
        Module module = scriptManager.loadFunction(id);
        int depth = 0;
    }

    //    private Module recursiveFindTriple(int depth, Module module) {
    //        if (module.getInputModules().size() > 0) {
    //            module.getInputModules().forEach(m -> {
    //                recursiveFindTriple(depth + 1, m);
    //            });
    //        }
    //
    //    }

    public List<ModuleExecution> findTripleOrigin(String executionId, String pattern) {
        Transformation pipelineExecution = transformationDao.findByUri(Vocabulary.s_c_transformation + "/" + executionId);
        String scriptName = pipelineExecution.getName();
        Set<String> global = scriptManager.getGlobalScripts();
        //        Module module = scriptManager.loadFunction(id);
        //        recursiveFindTriple(0, )
        System.out.println(scriptName);
        return null;
    }
}
