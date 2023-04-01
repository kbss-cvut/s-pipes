package cz.cvut.spipes.debug.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.cvut.spipes.debug.dto.PipelineComparisonResultDto;
import cz.cvut.spipes.debug.model.ModuleExecution;
import cz.cvut.spipes.debug.model.PipelineExecution;
import cz.cvut.spipes.model.PipelineComparison;

@Component
public class PipelineComparisonDtoMapper {
    private final TransformationDtoMapper transformationDtoMapper;

    @Autowired
    public PipelineComparisonDtoMapper(TransformationDtoMapper transformationDtoMapper) {
        this.transformationDtoMapper = transformationDtoMapper;
    }

    public PipelineComparisonResultDto toDto(PipelineComparison comparison) {
        PipelineComparisonResultDto dto = new PipelineComparisonResultDto();
        PipelineExecution pipelineExecution = transformationDtoMapper.transformationToPipelineExecution(comparison.getPipeline());
        PipelineExecution comparedTo = transformationDtoMapper.transformationToPipelineExecution(comparison.getCompare_to());
        Boolean areSame = comparison.getAre_same();
        ModuleExecution moduleExecution = transformationDtoMapper.transformationToModuleExecutionShort(comparison.getDifference_found_in());
        dto.setPipeline(pipelineExecution);
        dto.setCompare_to(comparedTo);
        dto.setAre_same(areSame);
        dto.setDifference_found_in(moduleExecution);
        dto.setId(comparison.getId());
        dto.setTypes(moduleExecution.getTypes());
        return dto;
    }
}
