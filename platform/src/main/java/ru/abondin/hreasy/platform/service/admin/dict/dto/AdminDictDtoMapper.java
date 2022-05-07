package ru.abondin.hreasy.platform.service.admin.dict.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.abondin.hreasy.platform.repo.dict.*;
import ru.abondin.hreasy.platform.service.dict.dto.DepartmentDto;
import ru.abondin.hreasy.platform.service.dict.dto.DictLevelDto;
import ru.abondin.hreasy.platform.service.dict.dto.DictOfficeLocationDto;
import ru.abondin.hreasy.platform.service.dict.dto.DictPositionDto;

@Mapper(componentModel = "spring")
public interface AdminDictDtoMapper  {

    DictOfficeLocationEntry toEntry(CreateOrUpdateOfficeLocationBody dto);

    DepartmentEntry toEntry(CreateOrUpdateDepartmentBody dto);

    DictLevelEntry toEntry(CreateOrUpdateLevelBody dto);

    DictPositionEntry toEntry(CreateOrUpdatePositionBody dto);

    @Mapping(source = "id", target = "officeLocationId")
    DictOfficeLocationLogEntry toHistory(DictOfficeLocationEntry entry);

    @Mapping(source = "id", target = "departmentId")
    DepartmentLogEntry toHistory(DepartmentEntry entry);

    @Mapping(source = "id", target = "levelId")
    DictLevelLogEntry toHistory(DictLevelEntry entry);

    @Mapping(source = "id", target = "positionId")
    DictPositionLogEntry toHistory(DictPositionEntry entry);

    DictOfficeLocationDto fromEntry(DictOfficeLocationEntry entry);

    DepartmentDto fromEntry(DepartmentEntry entry);

    DictLevelDto fromEntry(DictLevelEntry entry);

    DictPositionDto fromEntry(DictPositionEntry entry);
}
