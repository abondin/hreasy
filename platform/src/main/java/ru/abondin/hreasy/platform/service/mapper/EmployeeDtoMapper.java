package ru.abondin.hreasy.platform.service.mapper;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.abondin.hreasy.platform.repo.employee.EmployeeDetailedEntry;
import ru.abondin.hreasy.platform.service.dto.EmployeeDto;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Map all dictionaries database entries to API DTO
 */
@Mapper(componentModel = "spring")
public interface EmployeeDtoMapper {

    @Mapping(target = "displayName", source = ".", qualifiedByName = "displayName")
    @Mapping(target = "department", source = ".", qualifiedByName = "department")
    @Mapping(target = "currentProject", source = ".", qualifiedByName = "currentProject")
    @Mapping(target = "position", source = ".", qualifiedByName = "position")
    @Mapping(target = "officeLocation", source = ".", qualifiedByName = "officeLocation")
    @Mapping(target = "hasAvatar", ignore = true)
    EmployeeDto employeeToDto(EmployeeDetailedEntry entry);

    @Named("displayName")
    default String displayName(EmployeeDetailedEntry entry) {
        return entry == null ? null : Stream.of(
                entry.getLastname(),
                entry.getFirstname(),
                entry.getPatronymicName())
                .filter(s -> StringUtils.isNotBlank(s))
                .collect(Collectors.joining(" "));
    }

    @Named("department")
    default SimpleDictDto department(EmployeeDetailedEntry entry) {
        return simpleDto(entry.getDepartmentId(), entry.getDepartmentName());
    }

    @Named("position")
    default SimpleDictDto position(EmployeeDetailedEntry entry) {
        return simpleDto(entry.getPositionId(), entry.getPositionCategory() == null ? entry.getPositionName() : entry.getPositionCategory());
    }

    @Named("officeLocation")
    default SimpleDictDto officeLocation(EmployeeDetailedEntry entry) {
        return simpleDto(entry.getOfficeLocationId(), entry.getOfficeLocationName());
    }

    @Named("currentProject")
    default SimpleDictDto currentProject(EmployeeDetailedEntry entry) {
        return simpleDto(entry.getCurrentProjectId(), entry.getCurrentProjectName());
    }

    default SimpleDictDto simpleDto(Integer id, String name) {
        return id == null ? null : new SimpleDictDto(id, name);
    }

}
