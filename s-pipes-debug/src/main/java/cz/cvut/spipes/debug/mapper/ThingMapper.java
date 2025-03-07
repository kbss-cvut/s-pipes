package cz.cvut.spipes.debug.mapper;

import org.mapstruct.Mapper;

import cz.cvut.spipes.debug.dto.ThingDto;
import cz.cvut.spipes.model.Thing;

@Mapper(componentModel = "spring")
public interface ThingMapper {
    ThingDto toDto(Thing thing);
}
