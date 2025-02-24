package ru.abondin.hreasy.platform.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.abondin.hreasy.platform.repo.employee.EmployeeDetailedEntry;
import ru.abondin.hreasy.platform.repo.employee.EmployeeProjectChangesEntry;
import ru.abondin.hreasy.platform.service.dto.*;
import ru.abondin.hreasy.platform.service.skills.dto.RatingsMapper;

/**
 * Map all dictionaries database entries to API DTO
 */
@Mapper(componentModel = "spring")
public interface EmployeeDtoMapper extends MapperBase, RatingsMapper {

    @Mapping(target = "department", source = ".", qualifiedByName = "department")
    @Mapping(target = "currentProject", source = ".", qualifiedByName = "currentProject")
    @Mapping(target = "position", source = ".", qualifiedByName = "position")
    @Mapping(target = "officeLocation", source = ".", qualifiedByName = "officeLocation")
    @Mapping(target = "ba", source = ".", qualifiedByName = "ba")
    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "hasAvatar", ignore = true)
    EmployeeDto employeeNoSkills(EmployeeDetailedEntry entry);

    @Mapping(target = "ba", source = ".", qualifiedByName = "projectChangesBa")
    @Mapping(target = "employee", source = ".", qualifiedByName = "projectChangesEmployee")
    @Mapping(target = "changedBy", source = ".", qualifiedByName = "projectChangesChangedBy")
    @Mapping(target = "project", source = ".", qualifiedByName = "projectChangesProject")
    EmployeeProjectChangesDto employeeProjectChangesFromEntry(EmployeeProjectChangesEntry employeeProjectChangesEntry);


    default EmployeeDto employeeWithSkills(EmployeeDetailedEntry entry, int loggedInEmployee) {
        var result = employeeNoSkills(entry);
        if (result != null) {
            result.setSkills(parseAssembledSkills(entry.getAggregatedSkills(), loggedInEmployee));
        }
        return result;
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
    default OfficeLocationDictDto officeLocation(EmployeeDetailedEntry entry) {
        return officeLocationDto(entry.getOfficeLocationId(), entry.getOfficeLocationName(), entry.getOfficeLocationOfficeId(), entry.getOfficeLocationMapName());
    }

    @Named("ba")
    default SimpleDictDto ba(EmployeeDetailedEntry entry) {
        return simpleDto(entry.getBaId(), entry.getBaName());
    }

    @Named("currentProject")
    default CurrentProjectDictDto currentProject(EmployeeDetailedEntry entry) {
        CurrentProjectDictDto currentProject = null;
        if (entry != null && entry.getCurrentProjectId() != null) {
            currentProject = new CurrentProjectDictDto(entry.getCurrentProjectId(),
                    entry.getCurrentProjectName(), entry.getCurrentProjectRole());
        }
        return currentProject;
    }

    @Named("projectChangesBa")
    default SimpleDictDto projectChangesBa(EmployeeProjectChangesEntry entry) {
        return simpleDto(entry.getBaId(), entry.getBaName());
    }

    @Named("projectChangesEmployee")
    default SimpleDictDto projectChangesEmployee(EmployeeProjectChangesEntry entry) {
        return simpleDto(entry.getEmployeeId(), entry.getEmployeeDisplayName());
    }

    @Named("projectChangesChangedBy")
    default SimpleDictDto projectChangesChangedBy(EmployeeProjectChangesEntry entry) {
        return simpleDto(entry.getChangedById(), entry.getChangedByDisplayName());
    }

    @Named("projectChangesProject")
    default CurrentProjectDictDto projectChangesProject(EmployeeProjectChangesEntry entry) {
        return currentProjectDto(entry.getProjectId(), entry.getProjectName(), entry.getProjectRole());
    }


}
