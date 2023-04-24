package ru.abondin.hreasy.platform.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.abondin.hreasy.platform.repo.dict.DictProjectEntry;
import ru.abondin.hreasy.platform.service.dto.ProjectDictDto;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

/**
 * Map all dictionaries database entries to API DTO
 */
@Mapper(componentModel = "spring")
public interface DictDtoMapper {

    @Mapping(target = "active", ignore = true)
    ProjectDictDto projectToDto(DictProjectEntry entry);

}
