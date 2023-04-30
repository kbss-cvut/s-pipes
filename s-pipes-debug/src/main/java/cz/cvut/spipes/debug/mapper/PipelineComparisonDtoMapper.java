package cz.cvut.spipes.debug.mapper;

import org.mapstruct.Mapper;

import cz.cvut.spipes.debug.dto.PipelineComparisonResultDto;
import cz.cvut.spipes.model.PipelineComparison;

@Mapper(componentModel = "spring", uses = {ModuleExecutionMapper.class, PipelineExecutionMapper.class})
public interface PipelineComparisonDtoMapper {
    PipelineComparisonResultDto toDto(PipelineComparison pipelineComparison);
}
