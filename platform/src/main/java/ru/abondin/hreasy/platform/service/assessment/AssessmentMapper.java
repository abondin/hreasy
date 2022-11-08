package ru.abondin.hreasy.platform.service.assessment;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.abondin.hreasy.platform.repo.assessment.AssessmentFormEntry;
import ru.abondin.hreasy.platform.repo.assessment.AssessmentViewEntry;
import ru.abondin.hreasy.platform.repo.assessment.EmployeeAssessmentEntry;
import ru.abondin.hreasy.platform.service.assessment.dto.AssessmentDto;
import ru.abondin.hreasy.platform.service.assessment.dto.AssessmentWithFormsAndFiles;
import ru.abondin.hreasy.platform.service.assessment.dto.EmployeeAssessmentsSummary;
import ru.abondin.hreasy.platform.service.dto.CurrentProjectDictDto;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;
import ru.abondin.hreasy.platform.service.mapper.MapperBase;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper(componentModel = "spring")
public interface AssessmentMapper extends MapperBase {

    @Mapping(source = "employeeId", target = "employeeId")
    @Mapping(source = ".", target = "displayName", qualifiedByName = "displayName")
    @Mapping(source = "id", target = "lastAssessmentId")
    @Mapping(source = "plannedDate", target = "lastAssessmentDate")
    @Mapping(target = "lastAssessmentCompletedDate", source = "completedAt", qualifiedByName = "toLocalDate")
    @Mapping(source = "employeeDateOfEmployment", target = "employeeDateOfEmployment")
    @Mapping(source = ".", target = "latestActivity", qualifiedByName = "latestActivity")
    @Mapping(source = ".", target = "currentProject", qualifiedByName = "currentProject")
    @Mapping(source = ".", target = "ba", qualifiedByName = "ba")
    EmployeeAssessmentsSummary shortInfo(EmployeeAssessmentEntry entry);

    @Mapping(source = ".", target = "createdBy", qualifiedByName = "createdBy")
    @Mapping(source = ".", target = "completedBy", qualifiedByName = "completedBy")
    @Mapping(source = ".", target = "canceledBy", qualifiedByName = "canceledBy")
    @Mapping(source = ".", target = "employee", qualifiedByName = "assessmentEmployeeName")
    AssessmentDto assessmentBaseFromEntry(AssessmentViewEntry assessmentEntry);

    @Mapping(source = "assessmentEntry.id", target = "id")
    @Mapping(source = "assessmentEntry.", target = "employee", qualifiedByName = "assessmentEmployeeName")
    @Mapping(source = "assessmentEntry.plannedDate", target = "plannedDate")
    @Mapping(source = "assessmentEntry.createdAt", target = "createdAt")
    @Mapping(source = "assessmentEntry.completedAt", target = "completedAt")
    @Mapping(source = "assessmentEntry.canceledAt", target = "canceledAt")
    @Mapping(source = "assessmentEntry", target = "createdBy", qualifiedByName = "createdBy")
    @Mapping(source = "assessmentEntry", target = "completedBy", qualifiedByName = "completedBy")
    @Mapping(source = "assessmentEntry", target = "canceledBy", qualifiedByName = "canceledBy")
    @Mapping(source = "forms", target = "forms")
    @Mapping(source = "attachmentsFilenames", target = "attachmentsFilenames")
    @Mapping(source = "attachmentsAccessToken", target = "attachmentsAccessToken")
    AssessmentWithFormsAndFiles assessmentWithFormsAndFiles(AssessmentViewEntry assessmentEntry,
                                                            List<AssessmentFormEntry> forms,
                                                            List<String> attachmentsFilenames,
                                                            String attachmentsAccessToken);


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
    default CurrentProjectDictDto currentProject(EmployeeAssessmentEntry entry) {
        return currentProjectDto(entry.getEmployeeCurrentProjectId(), entry.getEmployeeCurrentProjectName(), entry.getEmployeeCurrentProjectRole());
    }

    @Named("ba")
    default SimpleDictDto ba(EmployeeAssessmentEntry entry) {
        return simpleDto(entry.getBaId(), entry.getBaName());
    }


    @Named("assessmentEmployeeName")
    default SimpleDictDto assessmentEmployeeName(AssessmentViewEntry entry) {
        return entry == null ? null : simpleDto(entry.getEmployee(), entry.getEmployeeDisplayName());
    }

    @Named("createdBy")
    default SimpleDictDto createdBy(AssessmentViewEntry entry) {
        return entry == null ? null : simpleDto(entry.getCreatedBy(), entry.getCreatedByDisplayName());
    }

    @Named("completedBy")
    default SimpleDictDto completedBy(AssessmentViewEntry entry) {
        return entry == null ? null : simpleDto(entry.getCompletedBy(), entry.getCompletedByDisplayName());
    }

    @Named("canceledBy")
    default SimpleDictDto canceledBy(AssessmentViewEntry entry) {
        return entry == null ? null : simpleDto(entry.getCanceledBy(), entry.getCanceledByDisplayName());
    }

    @Named("toLocalDate")
    default LocalDate toLocalDate(OffsetDateTime dateTime) {
        return dateTime == null ? null : dateTime.toLocalDate();
    }


}
