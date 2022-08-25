package ru.abondin.hreasy.platform.service.admin.manager.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.abondin.hreasy.platform.repo.manager.ManagerEntry;
import ru.abondin.hreasy.platform.repo.manager.ManagerView;

import java.time.OffsetDateTime;

@Mapper(componentModel = "spring")
public interface ManagerMapper {

    @Mapping(source = "objectType", target = "responsibilityObject.type")
    @Mapping(source = "objectName", target = "responsibilityObject.name")
    @Mapping(source = "objectId", target = "responsibilityObject.id")
    @Mapping(source = "employee", target = "employee.id")
    @Mapping(source = "employeeDisplayName", target = "employee.name")
    @Mapping(source = "employeeActive", target = "employee.active")
    ManagerDto fromEntry(ManagerView entry);

    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "createdBy", target = "createdBy")
    @Mapping(source = "body.employee", target = "employee")
    @Mapping(source = "body.responsibilityObjectType", target = "objectType")
    @Mapping(source = "body.responsibilityObjectId", target = "objectId")
    @Mapping(source = "body.responsibilityType", target = "responsibilityType")
    @Mapping(source = "body.comment", target = "comment")
    ManagerEntry toEntry(CreateManagerBody body, OffsetDateTime createdAt, Integer createdBy);

    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "createdBy", target = "createdBy")
    @Mapping(source = "body.responsibilityObjectType", target = "objectType")
    @Mapping(source = "body.responsibilityObjectId", target = "objectId")
    @Mapping(source = "body.responsibilityType", target = "responsibilityType")
    @Mapping(source = "body.comment", target = "comment")
    ManagerEntry update(@MappingTarget ManagerEntry entry, UpdateManagerBody body, OffsetDateTime createdAt, Integer createdBy);
}
