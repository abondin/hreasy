package ru.abondin.hreasy.platform.service.admin.manager.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.abondin.hreasy.platform.repo.manager.ManagerView;

@Mapper(componentModel = "spring")
public interface ManagerMapper {

    @Mapping(source = "objectType", target = "responsibilityObject.type")
    @Mapping(source = "objectName", target = "responsibilityObject.name")
    @Mapping(source = "baId", target = "responsibilityObject.baId")
    @Mapping(source = "departmentId", target = "responsibilityObject.departmentId")
    @Mapping(source = "objectId", target = "responsibilityObject.id")
    @Mapping(source = "employee", target = "employee.id")
    @Mapping(source = "employeeDisplayName", target = "employee.name")
    @Mapping(source = "employeeActive", target = "employee.active")
    ManagerDto fromEntry(ManagerView entry);

}
