package cz.cvut.spipes.debug.service;

import static java.util.Comparator.comparing;
import static cz.cvut.spipes.debug.util.IdUtils.generatePipelineComparisonIri;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cz.cvut.spipes.debug.dto.PipelineComparisonResultDto;
import cz.cvut.spipes.debug.exception.NotFoundException;
import cz.cvut.spipes.debug.mapper.ModuleExecutionMapper;
import cz.cvut.spipes.debug.mapper.PipelineComparisonDtoMapper;
import cz.cvut.spipes.debug.mapper.PipelineExecutionMapper;
import cz.cvut.spipes.debug.dto.ModuleExecutionDto;
import cz.cvut.spipes.debug.dto.PipelineExecutionDto;
import cz.cvut.spipes.debug.persistance.dao.ComparisonResultDao;
import cz.cvut.spipes.debug.persistance.dao.ModuleExecutionDao;
import cz.cvut.spipes.debug.persistance.dao.PipelineExecutionDao;
import cz.cvut.spipes.debug.tree.ExecutionTree;
import cz.cvut.spipes.model.ModuleExecution;
import cz.cvut.spipes.model.PipelineComparison;
import cz.cvut.spipes.model.PipelineExecution;

@Service
public class DebugService {

    private final ModuleExecutionDao moduleExecutionDao;

    private final PipelineExecutionDao pipelineExecutionDao;

    private final ModuleExecutionMapper moduleExecutionMapper;

    private final PipelineExecutionMapper pipelineExecutionMapper;

    private final PipelineComparisonDtoMapper pipelineComparisonDtoMapper;

    private final TreeService treeService;

    private final ComparisonResultDao comparisonResultDao;

    @Autowired
    public DebugService(
            ModuleExecutionDao moduleExecutionDao,
            PipelineExecutionDao pipelineExecutionDao,
            ModuleExecutionMapper moduleExecutionMapper, PipelineExecutionMapper pipelineExecutionMapper, TreeService treeService,
            ComparisonResultDao comparisonResultDao, PipelineComparisonDtoMapper pipelineComparisonDtoMapper) {
        this.moduleExecutionMapper = moduleExecutionMapper;
        this.pipelineExecutionMapper = pipelineExecutionMapper;
        this.moduleExecutionDao = moduleExecutionDao;
        this.pipelineExecutionDao = pipelineExecutionDao;
        this.treeService = treeService;
        this.comparisonResultDao = comparisonResultDao;
        this.pipelineComparisonDtoMapper = pipelineComparisonDtoMapper;
    }

    public List<PipelineExecutionDto> getAllPipelineExecutions() {
        List<PipelineExecution> pipelineExecutionDtos = pipelineExecutionDao.findAll();
        return pipelineExecutionDtos.stream()
                .sorted(comparing(PipelineExecution::getHas_pipepline_execution_start_date, Comparator.reverseOrder()))
                .map(pipelineExecutionMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ModuleExecutionDto> getAllModuleExecutionsSorted(String executionId, String orderBy, String orderType) {
        PipelineExecution pipelineExecution = pipelineExecutionDao.findById(executionId);
        if (pipelineExecution == null) {
            throw new NotFoundException("Pipeline execution with id " + executionId + " was not found");
        }
        Set<ModuleExecution> modules = pipelineExecution.getHas_part();
        modules.forEach(module -> {
            if (module.getStart_date() != null && module.getFinish_date() != null) {
                module.setDuration(getFormattedDuration(module));
            }
        });
        return getSortedModules(modules, orderBy, orderType)
                .stream()
                .map(moduleExecutionMapper::toDto)
                .collect(Collectors.toList());
    }

    public PipelineExecutionDto getPipelineExecutionById(String executionId) {
        PipelineExecution pipelineExecution = pipelineExecutionDao.findById(executionId);
        if (pipelineExecution == null) {
            throw new NotFoundException("Pipeline execution with id " + executionId + " was not found");
        }
        return pipelineExecutionMapper.toDto(pipelineExecution);
    }

    public PipelineComparisonResultDto compareExecutions(String executionId, String executionToCompareId) {
        PipelineExecution firstPipelineExecution = pipelineExecutionDao.findById(executionId);
        PipelineExecution secondPipelineExecution = pipelineExecutionDao.findById(executionToCompareId);
        if (firstPipelineExecution == null || secondPipelineExecution == null) {
            throw new NotFoundException("Pipeline execution(s) not found, check the id's");
        }
        PipelineComparison result = comparisonResultDao.findByCompareAndCompareWith(firstPipelineExecution.getId(), secondPipelineExecution.getId());
        if (result != null) {
            return pipelineComparisonDtoMapper.toDto(result);
        }
        ExecutionTree tree1 = new ExecutionTree(firstPipelineExecution.getHas_part());
        ExecutionTree tree2 = new ExecutionTree(secondPipelineExecution.getHas_part());
        ModuleExecution differenceIn = treeService.findFirstOutputDifference(tree1, tree2);
        result = buildPipelineComparisonResult(firstPipelineExecution, secondPipelineExecution, differenceIn);
        comparisonResultDao.persist(result);
        return pipelineComparisonDtoMapper.toDto(result);
    }


    private List<ModuleExecution> getSortedModules(Set<ModuleExecution> modules, String orderBy, String orderType) {
        Comparator<ModuleExecution> comparator;
        if (orderBy == null) {
            comparator = defaultComparator();
        } else {
            switch (orderBy) {
                case "duration":
                    comparator = comparing(ModuleExecution::getDuration);
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

    private PipelineComparison buildPipelineComparisonResult(PipelineExecution pipelineExecution, PipelineExecution compareTo, ModuleExecution difference) {
        PipelineComparison result = new PipelineComparison();
        result.setPipeline(pipelineExecution);
        result.setCompare_to(compareTo);
        result.setId(generatePipelineComparisonIri());
        if (difference == null) {
            result.setAre_same(true);
            return result;
        }
        result.setDifference_found_in(difference);
        return result;
    }

    private long getFormattedDuration(ModuleExecution moduleExecution) {
        Duration duration = Duration.between(moduleExecution.getStart_date().toInstant(),
                moduleExecution.getFinish_date().toInstant());
        return duration.toMillis();
    }

    private Comparator<ModuleExecution> defaultComparator() {
        return comparing(ModuleExecution::getStart_date);
    }
}