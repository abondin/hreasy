package ru.abondin.hreasy.platform.tg.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.abondin.hreasy.platform.service.dto.EmployeeDto;

@Mapper(componentModel = "spring")
public interface TgMapper {
    @Mapping(target = "projectId", source = "currentProject.id")
    @Mapping(target = "projectName", source = "currentProject.name")
    // Populated separately
    @Mapping(target = "projectManagers", ignore = true)
    @Mapping(target = "upcomingVacations", ignore = true)
    TgMyProfileResponse myProfile(EmployeeDto employee);

    @Mapping(target = "projectId", source = "currentProject.id")
    @Mapping(target = "projectName", source = "currentProject.name")
    @Mapping(target = "projectRole", source = "currentProject.role")
    @Mapping(target = "officeLocation", source = "officeLocation.name")
    @Mapping(target = "score", ignore = true)
    @Mapping(target = "upcomingVacations", ignore = true)
    @Mapping(target = "telegram", expression = "java(ru.abondin.hreasy.platform.tg.TelegramLinkNormalizer.normalizeTelegramLink(employeeDto.getTelegram()))")
    FindEmployeeResponse.EmployeeDto toFindEmployeeResponseEmployee(EmployeeDto employeeDto);

}
