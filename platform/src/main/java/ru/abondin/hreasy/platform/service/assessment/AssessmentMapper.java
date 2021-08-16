package ru.abondin.hreasy.platform.service.assessment;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.abondin.hreasy.platform.repo.assessment.AssessmentViewEntry;
import ru.abondin.hreasy.platform.repo.assessment.EmployeeAssessmentEntry;
import ru.abondin.hreasy.platform.service.assessment.dto.AssessmentDto;
import ru.abondin.hreasy.platform.service.assessment.dto.EmployeeAssessmentsSummary;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;
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
    @Mapping(source = ".", target = "currentProject", qualifiedByName = "currentProject")
    EmployeeAssessmentsSummary shortInfo(EmployeeAssessmentEntry entry);

    @Named("displayName")
    default String displayName(EmployeeAssessmentEntry entry) {
        return entry == null ? null : Stream.of(
                        entry.getEmployeeLastname(),
                        entry.getEmployeeFirstname(),
                        entry.getEmployeePatronymicName())
                .filter(s -> StringUtils.isNotBlank(s))
                .collect(Collectors.joining(" "));
    }

    @Mapping(source = ".", target = "createdBy", qualifiedByName = "createdBy")
    @Mapping(source = ".", target = "completedBy", qualifiedByName = "completedBy")
    @Mapping(source = ".", target = "canceledBy", qualifiedByName = "canceledBy")
    AssessmentDto assessmentBaseFromEntry(AssessmentViewEntry assessmentEntry);


    @Named("latestActivity")
    default LocalDate latestActivity(EmployeeAssessmentEntry entry) {
        if (entry.getEmployeeDateOfEmployment() != null) {
            return entry.getPlannedDate() == null ? entry.getEmployeeDateOfEmployment() :
                    (entry.getEmployeeDateOfEmployment().compareTo(entry.getPlannedDate()) >= 0 ? entry.getEmployeeDateOfEmployment() : entry.getPlannedDate());
        } else {
            return entry.getPlannedDate();
        }
    }

    @Named("currentProject")
    default SimpleDictDto currentProject(EmployeeAssessmentEntry entry) {
        return simpleDto(entry.getEmployeeCurrentProjectId(), entry.getEmployeeCurrentProjectName());
    }

    @Named("createdBy")
    default SimpleDictDto createdBy(AssessmentViewEntry entry) {
        return entry == null ? null : simpleDto(entry.getCreatedBy(), Stream.of(
                        entry.getCreatedByLastname(),
                        entry.getCreatedByFirstname(),
                        entry.getCreatedByPatronymicName())
                .filter(s -> StringUtils.isNotBlank(s))
                .collect(Collectors.joining(" ")));
    }

    @Named("completedBy")
    default SimpleDictDto completedBy(AssessmentViewEntry entry) {
        return entry == null ? null : simpleDto(entry.getCompletedBy(), Stream.of(
                        entry.getCompletedByLastname(),
                        entry.getCompletedByFirstname(),
                        entry.getCompletedByPatronymicName())
                .filter(s -> StringUtils.isNotBlank(s))
                .collect(Collectors.joining(" ")));
    }

    @Named("canceledBy")
    default SimpleDictDto canceledBy(AssessmentViewEntry entry) {
        return entry == null  ? null : simpleDto(entry.getCanceledBy(), Stream.of(
                        entry.getCanceledByLastname(),
                        entry.getCanceledByFirstname(),
                        entry.getCanceledByPatronymicName())
                .filter(s -> StringUtils.isNotBlank(s))
                .collect(Collectors.joining(" ")));
    }


}
