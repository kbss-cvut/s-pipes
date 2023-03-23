package cz.cvut.spipes.debug.service;

import static java.util.Comparator.comparing;
import static cz.cvut.spipes.debug.util.DebugUtils.getTransformationIriFromId;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cz.cvut.spipes.Vocabulary;
import cz.cvut.spipes.debug.mapper.DtoMapper;
import cz.cvut.spipes.debug.model.ModuleExecution;
import cz.cvut.spipes.debug.model.PipelineExecution;
import cz.cvut.spipes.debug.persistance.dao.TransformationDao;
import cz.cvut.spipes.debug.tree.ExecutionTree;
import cz.cvut.spipes.model.Transformation;

@Service
public class DebugService {

    private final TransformationDao transformationDao;

    private final DtoMapper dtoMapper;

    private final RelatedResourceService relatedResourceService;

    private final TreeService treeService;

    private static final String EXECUTION_IRI_PATTERN = "^http://onto\\.fel\\.cvut\\.cz/ontologies/dataset-descriptor/transformation/\\d+$";
    private static final String MODEL_IRI_PATTERN = "^http://onto\\.fel\\.cvut\\.cz/ontologies/dataset-descriptor/transformation/%s-\\d+-\\d+$";

    @Autowired
    public DebugService(TransformationDao transformationDao, DtoMapper dtoMapper, RelatedResourceService relatedResourceService, TreeService treeService) {
        this.transformationDao = transformationDao;
        this.dtoMapper = dtoMapper;
        this.relatedResourceService = relatedResourceService;
        this.treeService = treeService;
    }

    public List<PipelineExecution> getAllPipelineExecutions() {
        List<Transformation> transformations = transformationDao.findAll();

        List<PipelineExecution> pipelineExecutions = transformations.stream()
                .filter(transformation -> matchesExecutionPattern(transformation.getId()))
                .sorted(comparing(Transformation::getHas_pipepline_execution_date, Comparator.reverseOrder()))
                .map(dtoMapper::transformationToPipelineExecutionShort)
                .collect(Collectors.toList());

        pipelineExecutions.forEach(relatedResourceService::addPipelineExecutionResources);
        return pipelineExecutions;
    }

    public List<ModuleExecution> getAllModulesForExecutionWithExecutionTime(String executionId, String orderBy, String orderType) {
        Transformation pipelineTransformation = transformationDao.findByUri(Vocabulary.s_c_transformation + "/" + executionId);
        PipelineExecution pipelineExecution = dtoMapper.transformationToPipelineExecution(pipelineTransformation);
        List<ModuleExecution> modules = getModulesByExecutionId(executionId, pipelineExecution.getId());

        modules.forEach(module -> {
            if (module.getStart_date() != null && module.getFinish_date() != null) {
                module.setExecution_time_ms(getFormattedDuration(module));
            }
        });
        return getSortedModules(modules, orderBy, orderType);
    }


    private List<ModuleExecution> getModulesByExecutionId(String executionId, String pipelineExecutionIri) {
        List<ModuleExecution> modules = transformationDao.findAll().stream()
                .filter(transformation -> matchesModelPattern(executionId, transformation.getId()))
                .map(dtoMapper::transformationToModuleExecution)
                .collect(Collectors.toList());
        modules.forEach(module -> {
            module.setExecuted_in(pipelineExecutionIri);
            relatedResourceService.addModuleExecutionResources(module);
        });
        return modules;
    }

    private List<ModuleExecution> getSortedModules(List<ModuleExecution> modules, String orderBy, String orderType) {
        Comparator<ModuleExecution> comparator;
        switch (orderBy) {
            case "duration":
                comparator = comparing(ModuleExecution::getExecution_time_ms);
                break;
            case "output-triples":
                comparator = comparing(ModuleExecution::getOutput_triple_count);
                break;
            case "start-time":
                comparator = comparing(ModuleExecution::getStart_date);
                break;
            default:
                comparator = comparing(ModuleExecution::getStart_date);
                break;
        }
        if ("DESC".equalsIgnoreCase(orderType)) {
            comparator = comparator.reversed();
        }
        return modules.stream().sorted(comparator).collect(Collectors.toList());
    }

    public PipelineExecution getPipelineExecutionById(String executionId) {
        List<Transformation> transformations = transformationDao.findAll();
        List<ModuleExecution> modules = transformations.stream()
                .filter(transformation -> matchesModelPattern(executionId, transformation.getId()))
                .map(dtoMapper::transformationToModuleExecution)
                .collect(Collectors.toList());

        Transformation pipelineTransformation = transformationDao.findByUri(Vocabulary.s_c_transformation + "/" + executionId);
        PipelineExecution pipelineExecution = dtoMapper.transformationToPipelineExecution(pipelineTransformation);
        pipelineExecution.setHas_module_executions(modules);
        relatedResourceService.addPipelineExecutionResources(pipelineExecution);
        return pipelineExecution;
    }


    public ModuleExecution compareExecutions(String executionId, String executionToCompareId) {
        Transformation firstPipelineExecution = transformationDao.findByUri(getTransformationIriFromId(executionId));
        Transformation secondPipelineExecution = transformationDao.findByUri(getTransformationIriFromId(executionToCompareId));
        List<ModuleExecution> moduleExecutions1 = getModuleExecutionsFromPipelineTransformation(firstPipelineExecution);
        List<ModuleExecution> moduleExecutions2 = getModuleExecutionsFromPipelineTransformation(secondPipelineExecution);
        ExecutionTree tree1 = new ExecutionTree(moduleExecutions1);
        ExecutionTree tree2 = new ExecutionTree(moduleExecutions2);
        return treeService.findFirstOutputDifference(tree1, tree2);
    }

    private List<ModuleExecution> getModuleExecutionsFromPipelineTransformation(Transformation pipelineTransformation) {
        return pipelineTransformation.getHas_part().stream()
                .map(dtoMapper::transformationToModuleExecution)
                .collect(Collectors.toList());
    }

    private long getFormattedDuration(ModuleExecution moduleExecution) {
        Duration duration = Duration.between(moduleExecution.getStart_date().toInstant(),
                moduleExecution.getFinish_date().toInstant());
        return duration.toMillis();
    }

    private boolean matchesExecutionPattern(String id) {
        return id.matches(EXECUTION_IRI_PATTERN);

    }

    private boolean matchesModelPattern(String executionId, String potentialModuleId) {
        return potentialModuleId.matches(String.format(MODEL_IRI_PATTERN, executionId));
    }
}