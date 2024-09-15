package ru.abondin.hreasy.platform.service.admin.dict.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.abondin.hreasy.platform.repo.dict.*;
import ru.abondin.hreasy.platform.service.dict.dto.*;
import ru.abondin.hreasy.platform.service.mapper.MapperBase;

@Mapper(componentModel = "spring")
public interface AdminDictDtoMapper extends MapperBase {

    DictOfficeEntry toEntry(CreateOrUpdateOfficeBody dto);

    DictOfficeLocationEntry toEntry(CreateOrUpdateOfficeLocationBody dto);

    DictOfficeWorkplaceEntry toEntry(CreateOrUpdateWorkplaceBody dto);


    DictOrganizationEntry toEntry(CreateOrUpdateOrganizationBody dto);

    DepartmentEntry toEntry(CreateOrUpdateDepartmentBody dto);

    DictLevelEntry toEntry(CreateOrUpdateLevelBody dto);

    DictPositionEntry toEntry(CreateOrUpdatePositionBody dto);

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

    DictOfficeDto fromEntry(DictOfficeEntry entry);

    @Mapping(target = "office", expression = "java(simpleDto(entry.getOfficeId(), entry.getOfficeName()))")
    DictOfficeLocationDto fromEntry(DictOfficeLocationView entry);

    @Mapping(target = "office", expression = "java(simpleDto(entry.getOfficeId(), entry.getOfficeName()))")
    @Mapping(target = "officeLocation", expression = "java(simpleDto(entry.getOfficeLocationId(), entry.getOfficeLocationName()))")
    @Mapping(target = "mapPosition", expression = "java(position(entry.getMapX(), entry.getMapY()))")
    DictOfficeWorkplaceDto fromEntry(DictOfficeWorkplaceView entry);

    DictOrganizationDto fromEntry(DictOrganizationEntry entry);

    DepartmentDto fromEntry(DepartmentEntry entry);

    DictLevelDto fromEntry(DictLevelEntry entry);

    DictPositionDto fromEntry(DictPositionEntry entry);
}
