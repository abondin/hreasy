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

    SalaryRequestClosedPeriodDto closedPeriodFromEntry(SalaryRequestClosedPeriodEntry salaryRequestClosedPeriodEntry);


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

    @Named("employeeDepartment")
    default SimpleDictDto employeeDepartment(SalaryRequestView entry) {
        return simpleDto(entry.getEmployeeDepartmentId(), entry.getEmployeeDepartmentName());
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
        return simpleDto(entry.getImplementedBy(), entry.getImplementedDisplayName());
    }

// </editor-fold>


}