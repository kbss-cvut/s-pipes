package cz.cvut.spipes.debug.service;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import cz.cvut.spipes.Vocabulary;
import cz.cvut.spipes.debug.mapper.DtoMapper;
import cz.cvut.spipes.debug.model.ModuleExecution;
import cz.cvut.spipes.debug.model.PipelineExecution;
import cz.cvut.spipes.debug.persistance.dao.TransformationDao;
import cz.cvut.spipes.model.Transformation;

@Service
public class DebugService {

    private final TransformationDao transformationDao;

    private final DtoMapper dtoMapper;

    private final RelatedResourceService relatedResourceService;

    private static final String EXECUTION_IRI_PATTERN = "^http://onto\\.fel\\.cvut\\.cz/ontologies/dataset-descriptor/transformation/\\d+$";
    private static final String MODEL_IRI_PATTERN = "^http://onto\\.fel\\.cvut\\.cz/ontologies/dataset-descriptor/transformation/%s-\\d+-\\d+$";

    public DebugService(TransformationDao transformationDao, DtoMapper dtoMapper, RelatedResourceService relatedResourceService) {
        this.transformationDao = transformationDao;
        this.dtoMapper = dtoMapper;
        this.relatedResourceService = relatedResourceService;
    }

    public List<PipelineExecution> getAllPipelineExecutions() {
        List<Transformation> transformations = transformationDao.findAll();

        List<PipelineExecution> pipelineExecutions = transformations.stream()
                .filter(transformation -> matchesExecutionPattern(transformation.getId()))
                .sorted(Comparator.comparing(Transformation::getHas_pipepline_execution_date, Comparator.reverseOrder()))
                .map(dtoMapper::transformationToPipelineExecutionShort)
                .collect(Collectors.toList());

        pipelineExecutions.forEach(relatedResourceService::addPipelineExecutionResources);
        return pipelineExecutions;
    }

    public List<ModuleExecution> getAllModulesForExecution(String executionId, String orderBy) {
        Transformation pipelineTransformation = transformationDao.findByUri(Vocabulary.s_c_transformation + "/" + executionId);
        PipelineExecution pipelineExecution = dtoMapper.transformationToPipelineExecution(pipelineTransformation);

        List<ModuleExecution> modules = transformationDao.findAll().stream()
                .filter(transformation -> matchesModelPattern(executionId, transformation.getId()))
                .map(dtoMapper::transformationToModuleExecution)
                .collect(Collectors.toList());

        modules.forEach(module -> {
            module.setExecuted_in(pipelineExecution.getId());
            if (module.getStart_date() != null && module.getFinish_date() != null) {
                module.setExecution_time_ms(getFormattedDuration(module));
            }
            relatedResourceService.addModuleExecutionResources(module);
        });
        return getSortedModules(modules, orderBy);
    }

    private List<ModuleExecution> getSortedModules(List<ModuleExecution> modules, String orderBy) {
        if (orderBy == null || orderBy.equals("duration")) {
            return modules.stream()
                    .sorted(Comparator.comparing(ModuleExecution::getExecution_time_ms, Comparator.reverseOrder()))
                    .collect(Collectors.toList());
        }
        return modules.stream()
                .sorted(Comparator.comparing(ModuleExecution::getStart_date, Comparator.reverseOrder()))
                .collect(Collectors.toList());
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