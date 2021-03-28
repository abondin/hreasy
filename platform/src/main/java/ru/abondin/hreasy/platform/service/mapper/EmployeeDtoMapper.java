package ru.abondin.hreasy.platform.service.mapper;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.abondin.hreasy.platform.repo.employee.EmployeeDetailedEntry;
import ru.abondin.hreasy.platform.service.dto.EmployeeDto;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;
import ru.abondin.hreasy.platform.service.skills.dto.SkillDto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Map all dictionaries database entries to API DTO
 */
@Mapper(componentModel = "spring")
public interface EmployeeDtoMapper extends MapperBase {

    String SKILLS_SEPARATOR = "|#|";
    String SKILL_ATTRS_SEPARATOR = "|$|";

    @Mapping(target = "displayName", source = ".", qualifiedByName = "displayName")
    @Mapping(target = "department", source = ".", qualifiedByName = "department")
    @Mapping(target = "currentProject", source = ".", qualifiedByName = "currentProject")
    @Mapping(target = "position", source = ".", qualifiedByName = "position")
    @Mapping(target = "officeLocation", source = ".", qualifiedByName = "officeLocation")
    @Mapping(target = "skills", source = ".", qualifiedByName = "skillsSummary")
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

    @Named("skillsSummary")
    default List<SkillDto> skillsSummary(EmployeeDetailedEntry entry) {
        var result = new ArrayList<SkillDto>();
        var allSkillsString = entry.getAggregatedSkills();
        if (StringUtils.isBlank(allSkillsString)) {
            return result;
        }
        for (var skillStr : StringUtils.splitByWholeSeparator(allSkillsString, SKILLS_SEPARATOR)) {
            var skillAttrs = StringUtils.splitByWholeSeparator(skillStr, SKILL_ATTRS_SEPARATOR);
            if (skillAttrs.length == 6) {
                var skillId = Integer.parseInt(skillAttrs[0]);
                var skillName = skillAttrs[1].trim();
                var groupId = Integer.parseInt(skillAttrs[2].trim());
                var groupName = skillAttrs[3].trim();
                var cnt = Integer.parseInt(skillAttrs[4]);
                var avg = Float.parseFloat(skillAttrs[5]);
                var skill = new SkillDto();
                skill.setGroup(new SimpleDictDto(groupId, groupName));
                skill.setId(skillId);
                skill.setName(skillName);
                skill.setAverageRating(avg);
                skill.setRatingsCount(cnt);
                result.add(skill);
            }
        }
        return result;
    }

}
