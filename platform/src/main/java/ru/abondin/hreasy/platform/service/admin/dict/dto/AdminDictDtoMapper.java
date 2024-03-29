package ru.abondin.hreasy.platform.service.admin.dict.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.abondin.hreasy.platform.repo.dict.*;
import ru.abondin.hreasy.platform.service.dict.dto.*;

@Mapper(componentModel = "spring")
public interface AdminDictDtoMapper {

    DictOfficeLocationEntry toEntry(CreateOrUpdateOfficeLocationBody dto);

    DictOrganizationEntry toEntry(CreateOrUpdateOrganizationBody dto);

    DepartmentEntry toEntry(CreateOrUpdateDepartmentBody dto);

    DictLevelEntry toEntry(CreateOrUpdateLevelBody dto);

    DictPositionEntry toEntry(CreateOrUpdatePositionBody dto);

    @Mapping(source = "id", target = "officeLocationId")
    @Mapping(target = "id", ignore = true)
    DictOfficeLocationLogEntry toHistory(DictOfficeLocationEntry entry);

    @Mapping(source = "id", target = "organizationId")
    @Mapping(target = "id", ignore = true)
    DictOrganizationLogEntry toHistory(DictOrganizationEntry entry);

    @Mapping(source = "id", target = "departmentId")
    @Mapping(target = "id", ignore = true)
    DepartmentLogEntry toHistory(DepartmentEntry entry);

    @Mapping(source = "id", target = "levelId")
    @Mapping(target = "id", ignore = true)
    DictLevelLogEntry toHistory(DictLevelEntry entry);

    @Mapping(source = "id", target = "positionId")
    @Mapping(target = "id", ignore = true)
    DictPositionLogEntry toHistory(DictPositionEntry entry);

    DictOfficeLocationDto fromEntry(DictOfficeLocationEntry entry);

    DictOrganizationDto fromEntry(DictOrganizationEntry entry);

    DepartmentDto fromEntry(DepartmentEntry entry);

    DictLevelDto fromEntry(DictLevelEntry entry);

    DictPositionDto fromEntry(DictPositionEntry entry);
}
