package cz.cvut.spipes.debug.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import cz.cvut.spipes.debug.dto.PipelineExecutionDto;
import cz.cvut.spipes.model.PipelineExecution;

@Mapper(componentModel = "spring", uses = {ModuleExecutionMapper.class, ThingMapper.class})
public interface PipelineExecutionMapper {

    @Mapping(source = "has_part", target = "has_module_executions")
    @Mapping(source = "has_pipeline_execution_finish_date", target = "has_pipeline_execution_finish_date")
    @Mapping(source = "has_pipeline_execution_status", target = "has_pipeline_execution_status")
    @Mapping(source = "has_pipeline_name", target = "has_pipeline_name")
    @Mapping(source = "has_executed_function_name", target = "has_executed_function_name")
    @Mapping(source = "has_executed_function_script_path", target = "has_executed_function_script_path")
    PipelineExecutionDto toDto(PipelineExecution pipelineExecution);
}
