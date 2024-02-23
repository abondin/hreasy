package ru.abondin.hreasy.platform.service.salary.dto;

import org.mapstruct.*;
import ru.abondin.hreasy.platform.repo.assessment.EmployeeAssessmentEntry;
import ru.abondin.hreasy.platform.repo.employee.EmployeeDetailedEntry;
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

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "createdBy", target = "createdBy")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "body.increaseAmount", target = "reqIncreaseAmount")
    @Mapping(source = "body.currentSalaryAmount", target = "infoCurrentSalaryAmount")
    @Mapping(source = "body.plannedSalaryAmount", target = "reqPlannedSalaryAmount")
    @Mapping(source = "body.increaseStartPeriod", target = "reqIncreaseStartPeriod")
    @Mapping(source = "body.reason", target = "reqReason")
    @Mapping(source = "body.comment", target = "reqComment")
    @Mapping(source = "empl.currentProjectId", target = "infoEmplProject")
    @Mapping(source = "empl.currentProjectRole", target = "infoEmplProjectRole")
    @Mapping(source = "empl.dateOfEmployment", target = "infoDateOfEmployment")
    @Mapping(source = "empl.baId", target = "infoEmplBa")
    @Mapping(source = "empl.positionId", target = "infoEmplPosition")
    SalaryRequestEntry toEntry(SalaryRequestReportBody body,
                               EmployeeDetailedEntry empl,
                               Integer createdBy, OffsetDateTime createdAt);

    @Mapping(source = ".", target = "employee", qualifiedByName = "employee")
    @Mapping(source = ".", target = "budgetBusinessAccount", qualifiedByName = "budgetBusinessAccount")
    @Mapping(source = ".", target = "assessment", qualifiedByName = "assessment")
    @Mapping(source = ".", target = "createdBy", qualifiedByName = "createdBy")
    @Mapping(source = ".", target = "employeeInfo.position", qualifiedByName = "employeePosition")
    @Mapping(source = ".", target = "employeeInfo.currentProject", qualifiedByName = "employeeCurrentProject")
    @Mapping(source = ".", target = "employeeInfo.ba", qualifiedByName = "employeeBusinessAccount")
    @Mapping(source = "infoDateOfEmployment", target = "employeeInfo.dateOfEmployment")
    @Mapping(source = "infoCurrentSalaryAmount", target = "employeeInfo.currentSalaryAmount")
    @Mapping(source = "reqPlannedSalaryAmount", target = "req.plannedSalaryAmount")
    @Mapping(source = "infoPreviousSalaryIncreaseText", target = "employeeInfo.infoPreviousSalaryIncreaseText")
    @Mapping(source = "reqIncreaseAmount", target = "req.increaseAmount")
    @Mapping(source = "reqIncreaseStartPeriod", target = "req.increaseStartPeriod")
    @Mapping(source = "reqReason", target = "req.reason")
    @Mapping(source = "reqComment", target = "req.comment")
    @Mapping(source = "implementedAt", target = "impl.implementedAt")
    @Mapping(source = ".", target = "impl.implementedBy", qualifiedByName = "implementedBy")
    @Mapping(source = "implIncreaseAmount", target = "impl.increaseAmount")
    @Mapping(source = "implSalaryAmount", target = "impl.salaryAmount")
    @Mapping(source = "implIncreaseStartPeriod", target = "impl.increaseStartPeriod")
    @Mapping(source = "implRejectReason", target = "impl.rejectReason")
    @Mapping(source = "implComment", target = "impl.comment")
    @Mapping(source = "implState", target = "impl.state")
    @Mapping(source = ".", target = "impl.newPosition", qualifiedByName = "employeeNewPosition")
    SalaryRequestDto fromEntry(SalaryRequestView entry);

    @Mapping(source = "req.increaseAmount", target = "reqIncreaseAmount")
    @Mapping(source = "req.plannedSalaryAmount", target = "reqPlannedSalaryAmount")
    @Mapping(source = "req.reason", target = "reqReason")
    @Mapping(source = "impl.increaseAmount", target = "implIncreaseAmount")
    @Mapping(source = "impl.salaryAmount", target = "implSalaryAmount")
    @Mapping(source = "impl.increaseStartPeriod", target = "implIncreaseStartPeriod", qualifiedByName = "period")
    @Mapping(source = "impl.rejectReason", target = "implRejectReason")
    @Mapping(source = "impl.state", target = "implState")
    @Mapping(source = "impl.newPosition.name", target = "implNewPosition")
    @Mapping(source = "impl.implementedBy.name", target = "implemented")
    @Mapping(source = "employee.name", target = "employee")
    @Mapping(source = "employeeInfo.currentProject.name", target = "employeeProject")
    @Mapping(source = "employeeInfo.currentProject.role", target = "employeeProjectRole")
    @Mapping(source = "employeeInfo.ba.name", target = "employeeBusinessAccount")
    @Mapping(source = "employeeInfo.currentSalaryAmount", target = "currentSalaryAmount")
    @Mapping(source = "createdBy.name", target = "createdBy")
    @Mapping(source = "employeeInfo.position.name", target = "employeePosition")
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

    @Mapping(source = "body.increaseAmount", target = "implIncreaseAmount")
    @Mapping(source = "body.salaryAmount", target = "implSalaryAmount")
    @Mapping(source = "body.increaseStartPeriod", target = "implIncreaseStartPeriod")
    @Mapping(source = "body.newPosition", target = "implNewPosition")
    @Mapping(source = "body.comment", target = "implComment")
    @Mapping(source = "implementedAt", target = "implementedAt")
    @Mapping(source = "implementedBy", target = "implementedBy")
    @Mapping(constant = "1", target = "implState")
    void applyRequestImplementBody(@MappingTarget SalaryRequestEntry entry, SalaryRequestImplementBody body, OffsetDateTime implementedAt, Integer implementedBy);

    @Mapping(source = "body.reason", target = "implRejectReason")
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
    @Named("employeeBusinessAccount")
    default SimpleDictDto employeeBusinessAccount(SalaryRequestView entry) {
        return simpleDto(entry.getInfoEmplBa(), entry.getInfoEmplBaName());
    }


    @Named("assessment")
    default SimpleDictDto assessment(SalaryRequestView entry) {
        //TODO Add local date format
        return simpleDto(entry.getAssessmentId(), entry.getAssessmentPlannedDate() == null ? null : entry.getAssessmentPlannedDate().toString());
    }


    @Named("employeePosition")
    default SimpleDictDto employeePosition(SalaryRequestView entry) {
        return simpleDto(entry.getInfoEmplPosition(), entry.getInfoEmplPositionName());
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
        return currentProjectDto(entry.getInfoEmplProject(), entry.getInfoEmplProjectName(), entry.getInfoEmplProjectRole());
    }

    @Named("period")
    default YearMonth period(Integer periodId){
        return MapperBase.fromPeriodId(periodId);
    }

// </editor-fold>


}
