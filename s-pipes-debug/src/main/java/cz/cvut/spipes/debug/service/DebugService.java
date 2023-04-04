package cz.cvut.spipes.debug.service;

import static java.util.Comparator.comparing;
import static cz.cvut.spipes.debug.util.IdUtils.generatePipelineComparisonIri;
import static cz.cvut.spipes.debug.util.IdUtils.getTransformationIriFromId;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cz.cvut.spipes.Vocabulary;
import cz.cvut.spipes.debug.dto.PipelineComparisonResultDto;
import cz.cvut.spipes.debug.exception.NotFoundException;
import cz.cvut.spipes.debug.mapper.PipelineComparisonDtoMapper;
import cz.cvut.spipes.debug.mapper.TransformationDtoMapper;
import cz.cvut.spipes.debug.model.ModuleExecution;
import cz.cvut.spipes.debug.model.PipelineExecution;
import cz.cvut.spipes.debug.persistance.dao.ComparisonResultDao;
import cz.cvut.spipes.debug.persistance.dao.TransformationDao;
import cz.cvut.spipes.debug.tree.ExecutionTree;
import cz.cvut.spipes.debug.util.IdUtils;
import cz.cvut.spipes.model.PipelineComparison;
import cz.cvut.spipes.model.Transformation;

@Service
public class DebugService {

    private final TransformationDao transformationDao;

    private final TransformationDtoMapper transformationDtoMapper;

    private final PipelineComparisonDtoMapper pipelineComparisonDtoMapper;

    private final RelatedResourceService relatedResourceService;

    private final TreeService treeService;

    private final ComparisonResultDao comparisonResultDao;

    private static final String EXECUTION_IRI_PATTERN = "^http://onto\\.fel\\.cvut\\.cz/ontologies/dataset-descriptor/transformation/\\d+$";

    @Autowired
    public DebugService(
            TransformationDao transformationDao, TransformationDtoMapper transformationDtoMapper, RelatedResourceService relatedResourceService, TreeService treeService,
            ComparisonResultDao comparisonResultDao, PipelineComparisonDtoMapper pipelineComparisonDtoMapper) {
        this.transformationDao = transformationDao;
        this.transformationDtoMapper = transformationDtoMapper;
        this.relatedResourceService = relatedResourceService;
        this.treeService = treeService;
        this.comparisonResultDao = comparisonResultDao;
        this.pipelineComparisonDtoMapper = pipelineComparisonDtoMapper;
    }

    public List<PipelineExecution> getAllPipelineExecutions() {
        List<Transformation> transformations = transformationDao.findAll();

        List<PipelineExecution> pipelineExecutions = transformations.stream()
                .filter(transformation -> matchesExecutionPattern(transformation.getId()))
                .sorted(comparing(Transformation::getHas_pipepline_execution_date, Comparator.reverseOrder()))
                .map(transformationDtoMapper::transformationToPipelineExecutionShort)
                .collect(Collectors.toList());

        pipelineExecutions.forEach(relatedResourceService::addPipelineExecutionResources);
        return pipelineExecutions;
    }

    public List<ModuleExecution> getAllModuleExecutionsSorted(String executionId, String orderBy, String orderType) {
        String iriString = IdUtils.getTransformationIriFromId(executionId);
        Transformation pipelineTransformation = transformationDao.findByUri(iriString);
        if (pipelineTransformation == null) {
            throw new NotFoundException("Pipeline execution " + iriString + " was not found");
        }
        List<ModuleExecution> modules = getModulesByExecutionId(pipelineTransformation);
        modules.forEach(module -> {
            if (module.getStart_date() != null && module.getFinish_date() != null) {
                module.setExecution_time_ms(getFormattedDuration(module));
            }
        });
        return getSortedModules(modules, orderBy, orderType);
    }

    public PipelineExecution getPipelineExecutionById(String executionId) {
        String iriString = getTransformationIriFromId(executionId);
        Transformation transformation = transformationDao.findByUri(iriString);
        if (transformation == null) {
            throw new NotFoundException("Pipeline execution with id " + iriString);
        }
        Set<Transformation> parts = transformation.getHas_part();
        List<ModuleExecution> modules = parts.stream()
                .map(transformationDtoMapper::transformationToModuleExecution)
                .collect(Collectors.toList());

        Transformation pipelineTransformation = transformationDao.findByUri(Vocabulary.s_c_transformation + "/" + executionId);
        PipelineExecution pipelineExecution = transformationDtoMapper.transformationToPipelineExecution(pipelineTransformation);
        pipelineExecution.setHas_module_executions(modules);
        relatedResourceService.addPipelineExecutionResources(pipelineExecution);
        return pipelineExecution;
    }

    public PipelineComparisonResultDto compareExecutions(String executionId, String executionToCompareId) {
        Transformation firstPipelineExecution = transformationDao.findByUri(getTransformationIriFromId(executionId));
        Transformation secondPipelineExecution = transformationDao.findByUri(getTransformationIriFromId(executionToCompareId));
        if (firstPipelineExecution == null || secondPipelineExecution == null) {
            throw new NotFoundException("Pipeline execution(s) not found, check the id's");
        }
        PipelineComparison result = comparisonResultDao.findByCompareAndCompareWith(firstPipelineExecution.getId(), secondPipelineExecution.getId());
        if (result != null) {
            return pipelineComparisonDtoMapper.toDto(result);
        }
        List<ModuleExecution> moduleExecutions1 = getModuleExecutionsFromPipelineTransformation(firstPipelineExecution);
        List<ModuleExecution> moduleExecutions2 = getModuleExecutionsFromPipelineTransformation(secondPipelineExecution);
        ExecutionTree tree1 = new ExecutionTree(moduleExecutions1);
        ExecutionTree tree2 = new ExecutionTree(moduleExecutions2);
        ModuleExecution differenceIn = treeService.findFirstOutputDifference(tree1, tree2);
        result = buildPipelineComparisonResult(firstPipelineExecution, secondPipelineExecution, differenceIn);
        comparisonResultDao.persist(result);
        return pipelineComparisonDtoMapper.toDto(result);
    }


    private List<ModuleExecution> getModulesByExecutionId(Transformation pipelineExecution) {
        List<ModuleExecution> modules = pipelineExecution.getHas_part().stream()
                .map(transformationDtoMapper::transformationToModuleExecution)
                .collect(Collectors.toList());
        modules.forEach(module -> {
            module.setExecuted_in(pipelineExecution.getId());
            relatedResourceService.addModuleExecutionResources(module);
        });
        return modules;
    }

    private List<ModuleExecution> getSortedModules(List<ModuleExecution> modules, String orderBy, String orderType) {
        Comparator<ModuleExecution> comparator;
        if (orderBy == null) {
            comparator = defaultComparator();
        } else {
            switch (orderBy) {
                case "duration":
                    comparator = comparing(ModuleExecution::getExecution_time_ms);
                    break;
                case "output-triples":
                    comparator = comparing(ModuleExecution::getOutput_triple_count);
                    break;
                case "input-triples":
                    comparator = comparing(ModuleExecution::getInput_triple_count);
                    break;
                case "start-time":
                    comparator = defaultComparator();
                    break;
                default:
                    comparator = defaultComparator();
                    break;
            }
        }
        if ("DESC".equalsIgnoreCase(orderType)) {
            comparator = comparator.reversed();
        }
        return modules.stream().sorted(comparator).collect(Collectors.toList());
    }

    private PipelineComparison buildPipelineComparisonResult(Transformation pipelineExecution, Transformation compareTo, ModuleExecution difference) {
        PipelineComparison result = new PipelineComparison();
        result.setPipeline(pipelineExecution);
        result.setCompare_to(compareTo);
        result.setId(generatePipelineComparisonIri());
        if (difference == null) {
            result.setAre_same(true);
            return result;
        }
        result.setAre_same(false);
        Transformation transformation = transformationDao.findByUri(difference.getId());
        result.setDifference_found_in(transformation);
        return result;
    }

    private List<ModuleExecution> getModuleExecutionsFromPipelineTransformation(Transformation pipelineTransformation) {
        return pipelineTransformation.getHas_part().stream()
                .map(transformationDtoMapper::transformationToModuleExecution)
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

    private Comparator<ModuleExecution> defaultComparator() {
        return comparing(ModuleExecution::getStart_date);
    }
}