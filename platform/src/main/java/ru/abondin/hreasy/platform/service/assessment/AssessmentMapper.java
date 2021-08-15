package ru.abondin.hreasy.platform.service.assessment;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.abondin.hreasy.platform.repo.assessment.EmployeeAssessmentEntry;
import ru.abondin.hreasy.platform.service.assessment.dto.AssessmentShortInfoDto;
import ru.abondin.hreasy.platform.service.mapper.MapperBase;

import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper(componentModel = "spring")
public interface AssessmentMapper extends MapperBase {

    @Mapping(source = "employeeId", target = "employeeId")
    @Mapping(source = ".", target = "displayName", qualifiedByName = "displayName")
    @Mapping(source = "id", target = "lastAssessmentId")
    @Mapping(source = "plannedDate", target = "lastAssessmentDate")
    @Mapping(source = "completedAt", target = "lastAssessmentCompletedDate")
    @Mapping(source = "employeeDateOfEmployment", target = "employeeDateOfEmployment")
    @Mapping(source = ".", target = "latestActivity", qualifiedByName = "latestActivity")
    AssessmentShortInfoDto shortInfo(EmployeeAssessmentEntry entry);

    @Named("displayName")
    default String displayName(EmployeeAssessmentEntry entry) {
        return entry == null ? null : Stream.of(
                        entry.getEmployeeLastname(),
                        entry.getEmployeeFirstname(),
                        entry.getEmployeePatronymicName())
                .filter(s -> StringUtils.isNotBlank(s))
                .collect(Collectors.joining(" "));
    }

    @Named("latestActivity")
    default LocalDate latestActivity(EmployeeAssessmentEntry entry) {
        if (entry.getEmployeeDateOfEmployment() != null) {
            return entry.getPlannedDate() == null ? entry.getEmployeeDateOfEmployment() :
                    (entry.getEmployeeDateOfEmployment().compareTo(entry.getPlannedDate()) >= 0 ? entry.getEmployeeDateOfEmployment() : entry.getPlannedDate());
        } else {
            return entry.getPlannedDate();
        }

    }
}
