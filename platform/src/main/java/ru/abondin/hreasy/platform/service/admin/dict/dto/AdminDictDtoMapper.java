package ru.abondin.hreasy.platform.service.admin.dict.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.abondin.hreasy.platform.repo.dict.*;
import ru.abondin.hreasy.platform.service.dict.dto.*;
import ru.abondin.hreasy.platform.service.mapper.MapperBase;

import java.time.OffsetDateTime;

@Mapper(componentModel = "spring")
public interface AdminDictDtoMapper extends MapperBase {

    DictOfficeEntry toEntry(CreateOrUpdateOfficeBody dto);

    DictOfficeLocationEntry toEntry(CreateOrUpdateOfficeLocationBody dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "officeLocationId", source = "officeLocationId")
    @Mapping(target = "createdBy", source = "author")
    @Mapping(target = "createdAt", source = "now")
    DictOfficeWorkplaceEntry toNewWorkplaceEntry(int officeLocationId,
                                                 CreateOrUpdateWorkplaceBody dto, int author, OffsetDateTime now);


    DictOfficeWorkplaceEntry applyWorkplaceBody(@MappingTarget DictOfficeWorkplaceEntry entry, CreateOrUpdateWorkplaceBody body);

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
    DictOfficeWorkplaceDto fromEntry(DictOfficeWorkplaceView entry);

    DictOrganizationDto fromEntry(DictOrganizationEntry entry);

    DepartmentDto fromEntry(DepartmentEntry entry);

    DictLevelDto fromEntry(DictLevelEntry entry);

    DictPositionDto fromEntry(DictPositionEntry entry);


}
