package ru.abondin.hreasy.platform.service.admin.manager.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.abondin.hreasy.platform.repo.manager.ManagerEntry;

@Mapper(componentModel = "spring")
public interface ManagerMapper {

    @Mapping(source = "objectType", target = "responsibilityObject.type")
    @Mapping(source = "objectId", target = "responsibilityObject.id")
    ManagerDto fromEntry(ManagerEntry entry);

}
