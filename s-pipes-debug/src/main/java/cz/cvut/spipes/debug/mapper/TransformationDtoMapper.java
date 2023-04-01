package cz.cvut.spipes.debug.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import cz.cvut.spipes.debug.model.ModuleExecution;
import cz.cvut.spipes.debug.model.PipelineExecution;
import cz.cvut.spipes.model.Transformation;

@Mapper(componentModel = "spring")
public interface TransformationDtoMapper {
    PipelineExecution transformationToPipelineExecution(Transformation transformation);

    @Mapping(target = "has_next", source = "has_next.id")
    ModuleExecution transformationToModuleExecution(Transformation transformation);

    PipelineExecution transformationToPipelineExecutionShort(Transformation transformation);

    @Mapping(target = "start_date", ignore = true)
    @Mapping(target = "finish_date", ignore = true)
    @Mapping(target = "output_triple_count", ignore = true)
    @Mapping(target = "has_rdf4j_output", ignore = true)
    @Mapping(target = "has_rdf4j_input", ignore = true)
    @Mapping(target = "has_next", ignore = true)
    ModuleExecution transformationToModuleExecutionShort(Transformation transformation);
}
