package ru.abondin.hreasy.platform.service.admin.dto;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.abondin.hreasy.platform.repo.employee.admin.EmployeeWithSecurityInfoEntry;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;
import ru.abondin.hreasy.platform.service.mapper.MapperBase;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper(componentModel = "spring")
public interface EmployeeWithSecurityMapper extends MapperBase {


    @Mapping(target = "employee", source = ".", qualifiedByName = "employeeDict")
    @Mapping(target = "accessibleDepartments", source = ".", qualifiedByName = "accessibleDepartments")
    @Mapping(target = "accessibleProjects", source = ".", qualifiedByName = "accessibleProjects")
    @Mapping(target = "roles", source = ".", qualifiedByName = "roles")
    EmployeeWithSecurityInfoDto fromEntry(EmployeeWithSecurityInfoEntry entry);


    @Named("accessibleDepartments")
    default List<Integer> accessibleDepartments(EmployeeWithSecurityInfoEntry entry) {
        return splitIds(entry.getAccessibleDepartments());
    }

    @Named("accessibleProjects")
    default List<Integer> accessibleProjects(EmployeeWithSecurityInfoEntry entry) {
        return splitIds(entry.getAccessibleProjects());
    }

    @Named("roles")
    default List<String> roles(EmployeeWithSecurityInfoEntry entry) {
        return splitStrings(entry.getRoles());
    }

    @Named("employeeDict")
    default SimpleDictDto employee(EmployeeWithSecurityInfoEntry entry) {
        var displayName = entry == null ? null : Stream.of(
                entry.getLastname(),
                entry.getFirstname(),
                entry.getPatronymicName())
                .filter(s -> StringUtils.isNotBlank(s))
                .collect(Collectors.joining(" "));
        return simpleDto(entry.getEmployeeId(), displayName);
    }

}
