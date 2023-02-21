package cz.cvut.spipes.debug.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import cz.cvut.spipes.debug.model.ModuleExecution;
import cz.cvut.spipes.debug.model.PipelineExecution;
import cz.cvut.spipes.model.Transformation;

@Mapper(componentModel = "spring")
public interface DtoMapper {
    PipelineExecution transformationToPipelineExecution(Transformation transformation);

    ModuleExecution transformationToModuleExecution(Transformation transformation);

    PipelineExecution transformationToPipelineExecutionShort(Transformation transformation);

    @Mapping(target = "start_date", ignore = true)
    @Mapping(target = "finish_date", ignore = true)
    ModuleExecution transformationToModuleExecutionShort(Transformation transformation);
}
