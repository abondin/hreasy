package ru.abondin.hreasy.platform.service.salary.dto;

import org.mapstruct.*;
import ru.abondin.hreasy.platform.repo.assessment.EmployeeAssessmentEntry;
import ru.abondin.hreasy.platform.repo.salary.SalaryRequestClosedPeriodEntry;
import ru.abondin.hreasy.platform.repo.salary.SalaryRequestEntry;
import ru.abondin.hreasy.platform.repo.salary.SalaryRequestView;
import ru.abondin.hreasy.platform.service.dto.CurrentProjectDictDto;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;
import ru.abondin.hreasy.platform.service.mapper.MapperBase;

import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.Locale;

@Mapper(componentModel = "spring")
public interface SalaryRequestMapper extends MapperBase {

    @Mapping(source = "createdBy", target = "createdBy")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "body.salaryIncrease", target = "reqSalaryIncrease")
    @Mapping(source = "body.increaseStartPeriod", target = "reqIncreaseStartPeriod")
    @Mapping(source = "body.reason", target = "reqReason")
    @Mapping(source = "body.comment", target = "reqComment")
    SalaryRequestEntry toEntry(SalaryRequestReportBody body, Integer createdBy, OffsetDateTime createdAt);

    @Mapping(source = ".", target = "employee", qualifiedByName = "employee")
    @Mapping(source = ".", target = "employeeCurrentProject", qualifiedByName = "employeeCurrentProject")
    @Mapping(source = ".", target = "budgetBusinessAccount", qualifiedByName = "budgetBusinessAccount")
    @Mapping(source = ".", target = "assessment", qualifiedByName = "assessment")
    @Mapping(source = ".", target = "employeePosition", qualifiedByName = "employeePosition")
    @Mapping(source = ".", target = "createdBy", qualifiedByName = "createdBy")
    @Mapping(source = "reqSalaryIncrease", target = "req.salaryIncrease")
    @Mapping(source = "reqIncreaseStartPeriod", target = "req.increaseStartPeriod")
    @Mapping(source = "reqReason", target = "req.reason")
    @Mapping(source = "reqComment", target = "req.comment")
    @Mapping(source = "implementedAt", target = "impl.implementedAt")
    @Mapping(source = ".", target = "impl.implementedBy", qualifiedByName = "implementedBy")
    @Mapping(source = "implSalaryIncrease", target = "impl.salaryIncrease")
    @Mapping(source = "implIncreaseStartPeriod", target = "impl.increaseStartPeriod")
    @Mapping(source = "implReason", target = "impl.reason")
    @Mapping(source = "implComment", target = "impl.comment")
    @Mapping(source = "implState", target = "impl.state")
    @Mapping(source = ".", target = "impl.newPosition", qualifiedByName = "employeeNewPosition")
    SalaryRequestDto fromEntry(SalaryRequestView entry);

    @Mapping(source = "req.salaryIncrease", target = "reqSalaryIncrease")
    @Mapping(source = "req.reason", target = "reqReason")
    @Mapping(source = "impl.salaryIncrease", target = "implSalaryIncrease")
    @Mapping(source = "impl.increaseStartPeriod", target = "implIncreaseStartPeriod", qualifiedByName = "period")
    @Mapping(source = "impl.reason", target = "implReason")
    @Mapping(source = "impl.state", target = "implState")
    @Mapping(source = "impl.newPosition.name", target = "implNewPosition")
    @Mapping(source = "impl.implementedBy.name", target = "implemented")
    @Mapping(source = "employee.name", target = "employee")
    @Mapping(source = "employeeCurrentProject.name", target = "employeeProject")
    @Mapping(source = "employeeCurrentProject.role", target = "employeeProjectRole")
    @Mapping(source = "createdBy.name", target = "createdBy")
    @Mapping(source = "employeePosition.name", target = "employeePosition")
    @Mapping(source = "budgetBusinessAccount.name", target = "budgetBusinessAccount")
    @Mapping(source = "assessment.name", target = "assessment")
    SalaryRequestExportDto toExportDto(SalaryRequestDto dto);


    @AfterMapping
    default void dtoAfterMapping(@MappingTarget SalaryRequestDto dto) {
        if (dto.getImpl() != null && dto.getImpl().getImplementedAt() == null) {
            dto.setImpl(null);
        }
    }

    SalaryRequestClosedPeriodDto closedPeriodFromEntry(SalaryRequestClosedPeriodEntry salaryRequestClosedPeriodEntry);

    @Mapping(source = "body.salaryIncrease", target = "implSalaryIncrease")
    @Mapping(source = "body.increaseStartPeriod", target = "implIncreaseStartPeriod")
    @Mapping(source = "body.newPosition", target = "implNewPosition")
    @Mapping(source = "body.reason", target = "implReason")
    @Mapping(source = "body.comment", target = "implComment")
    @Mapping(source = "implementedAt", target = "implementedAt")
    @Mapping(source = "implementedBy", target = "implementedBy")
    @Mapping(constant = "1", target = "implState")
    void applyRequestImplementBody(@MappingTarget SalaryRequestEntry entry, SalaryRequestImplementBody body, OffsetDateTime implementedAt, Integer implementedBy);

    @Mapping(source = "body.reason", target = "implReason")
    @Mapping(source = "body.comment", target = "implComment")
    @Mapping(source = "implementedAt", target = "implementedAt")
    @Mapping(source = "implementedBy", target = "implementedBy")
    @Mapping(constant = "2", target = "implState")
    void applyRequestRejectBody(@MappingTarget SalaryRequestEntry entry, SalaryRequestRejectBody body, OffsetDateTime implementedAt, Integer implementedBy);

// <editor-fold desc="named helpers">

    @Named("employee")
    default SimpleDictDto employee(SalaryRequestView entry) {
        return simpleDto(entry.getEmployeeId(), entry.getEmployeeDisplayName());
    }

    @Named("budgetBusinessAccount")
    default SimpleDictDto budgetBusinessAccount(SalaryRequestView entry) {
        return simpleDto(entry.getBudgetBusinessAccount(), entry.getBudgetBusinessAccountName());
    }

    @Named("assessment")
    default SimpleDictDto assessment(SalaryRequestView entry) {
        //TODO Add local date format
        return simpleDto(entry.getAssessmentId(), entry.getAssessmentPlannedDate() == null ? null : entry.getAssessmentPlannedDate().toString());
    }


    @Named("employeePosition")
    default SimpleDictDto employeePosition(SalaryRequestView entry) {
        return simpleDto(entry.getEmployeePositionId(), entry.getEmployeePositionName());
    }

    @Named("employeeNewPosition")
    default SimpleDictDto employeeNewPosition(SalaryRequestView entry) {
        return simpleDto(entry.getImplNewPosition(), entry.getImplNewPositionName());
    }

    @Named("createdBy")
    default SimpleDictDto createdBy(SalaryRequestView entry) {
        return simpleDto(entry.getCreatedBy(), entry.getCreatedByDisplayName());
    }


    @Named("implementedBy")
    default SimpleDictDto implementedBy(SalaryRequestView entry) {
        return simpleDto(entry.getImplementedBy(), entry.getImplementedByDisplayName());
    }

    @Named("employeeCurrentProject")
    default CurrentProjectDictDto currentProject(SalaryRequestView entry) {
        return currentProjectDto(entry.getEmployeeCurrentProjectId(), entry.getEmployeeCurrentProjectName(), entry.getEmployeeCurrentProjectRole());
    }

    @Named("period")
    default YearMonth period(Integer periodId){
        return MapperBase.fromPeriodId(periodId);
    }

// </editor-fold>


}
