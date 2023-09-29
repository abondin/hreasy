package ru.abondin.hreasy.platform.service.salary.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.abondin.hreasy.platform.repo.salary.SalaryRequestClosedPeriodEntry;
import ru.abondin.hreasy.platform.repo.salary.SalaryRequestEntry;
import ru.abondin.hreasy.platform.repo.salary.SalaryRequestView;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;
import ru.abondin.hreasy.platform.service.mapper.MapperBase;

import java.time.OffsetDateTime;

@Mapper(componentModel = "spring")
public interface SalaryRequestMapper extends MapperBase {

    @Mapping(source = "createdBy", target = "createdBy")
    @Mapping(source = "createdAt", target = "createdAt")
    SalaryRequestEntry toEntry(SalaryRequestReportBody body, Integer createdBy, OffsetDateTime createdAt);

    @Mapping(source = ".", target = "employee", qualifiedByName = "employee")
    @Mapping(source = ".", target = "budgetBusinessAccount", qualifiedByName = "budgetBusinessAccount")
    @Mapping(source = ".", target = "assessment", qualifiedByName = "assessment")
    @Mapping(source = ".", target = "employeeDepartment", qualifiedByName = "employeeDepartment")
    @Mapping(source = ".", target = "createdBy", qualifiedByName = "createdBy")
    @Mapping(source = ".", target = "rejectedBy", qualifiedByName = "rejectedBy")
    @Mapping(source = ".", target = "implementedBy", qualifiedByName = "implementedBy")
    @Mapping(source = ".", target = "stat", qualifiedByName = "calculateStat")
    SalaryRequestDto fromEntry(SalaryRequestView entry);

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

    @Named("employeeDepartment")
    default SimpleDictDto employeeDepartment(SalaryRequestView entry) {
        return simpleDto(entry.getEmployeeDepartmentId(), entry.getEmployeeDepartmentName());
    }

    @Named("createdBy")
    default SimpleDictDto createdBy(SalaryRequestView entry) {
        return simpleDto(entry.getCreatedBy(), entry.getCreatedByDisplayName());
    }

    @Named("rejectedBy")
    default SimpleDictDto rejectedBy(SalaryRequestView entry) {
        return simpleDto(entry.getRejectedBy(), entry.getInprgressDisplayName());
    }

    @Named("implementedBy")
    default SimpleDictDto implementedBy(SalaryRequestView entry) {
        return simpleDto(entry.getImplementedBy(), entry.getImplementedDisplayName());
    }

    @Named("calculateStat")
    default int calculateStat(SalaryRequestView entry) {
        return entry.getRejectedAt() != null ? SalaryRequestStat.REJECTED.getValue() :
                (
                        entry.getImplementedBy() != null ? SalaryRequestStat.IMPLEMENTED.getValue() : SalaryRequestStat.CREATED.getValue()
                );
    }

    SalaryRequestClosedPeriodDto closedPeriodFromEntry(SalaryRequestClosedPeriodEntry salaryRequestClosedPeriodEntry);
}
