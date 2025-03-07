package cz.cvut.spipes.debug.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import cz.cvut.spipes.debug.dto.PipelineExecutionDto;
import cz.cvut.spipes.model.PipelineExecution;

@Mapper(componentModel = "spring", uses = {ModuleExecutionMapper.class, ThingMapper.class})
public interface PipelineExecutionMapper {

    @Mapping(source = "has_part", target = "has_module_executions")
    PipelineExecutionDto toDto(PipelineExecution pipelineExecution);
}
