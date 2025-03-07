package cz.cvut.spipes.debug.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import cz.cvut.spipes.debug.dto.ModuleExecutionDto;
import cz.cvut.spipes.model.ModuleExecution;

@Mapper(componentModel = "spring", uses = {ThingMapper.class})
public interface ModuleExecutionMapper {
    @Mapping(target = "has_next", source = "has_next.id")
    ModuleExecutionDto toDto(ModuleExecution moduleExecution);
}
