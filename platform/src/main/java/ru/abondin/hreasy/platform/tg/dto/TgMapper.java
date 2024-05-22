package ru.abondin.hreasy.platform.tg.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.abondin.hreasy.platform.service.dto.EmployeeDto;

@Mapper(componentModel = "spring")
public interface TgMapper {
    @Mapping(target = "projectName", source = "employee.currentProject.name")
    TgMyProfileDto myProfile(EmployeeDto employee);
}
