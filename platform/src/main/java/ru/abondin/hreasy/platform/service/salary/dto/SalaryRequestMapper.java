package ru.abondin.hreasy.platform.service.salary.dto;

import io.r2dbc.postgresql.codec.Json;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import ru.abondin.hreasy.platform.I18Helper;
import ru.abondin.hreasy.platform.repo.employee.EmployeeDetailedEntry;
import ru.abondin.hreasy.platform.repo.salary.*;
import ru.abondin.hreasy.platform.service.dto.CurrentProjectDictDto;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;
import ru.abondin.hreasy.platform.service.mapper.MapperBase;
import ru.abondin.hreasy.platform.service.mapper.MapperBaseWithJsonSupport;
import ru.abondin.hreasy.platform.service.salary.dto.approval.SalaryRequestApprovalDto;

import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring")
public abstract class SalaryRequestMapper extends MapperBaseWithJsonSupport {


    @Autowired
    private I18Helper i18Helper;

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "createdBy", target = "createdBy")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "body.increaseAmount", target = "reqIncreaseAmount")
    @Mapping(source = "body.currentSalaryAmount", target = "infoCurrentSalaryAmount")
    @Mapping(source = "body.previousSalaryIncreaseDate", target = "infoPreviousSalaryIncreaseDate")
    @Mapping(source = "body.previousSalaryIncreaseText", target = "infoPreviousSalaryIncreaseText")
    @Mapping(source = "body.plannedSalaryAmount", target = "reqPlannedSalaryAmount")
    @Mapping(source = "body.increaseStartPeriod", target = "reqIncreaseStartPeriod")
    @Mapping(source = "body.reason", target = "reqReason")
    @Mapping(source = "body.comment", target = "reqComment")
    @Mapping(source = "empl.currentProjectId", target = "infoEmplProject")
    @Mapping(source = "empl.currentProjectRole", target = "infoEmplProjectRole")
    @Mapping(source = "empl.dateOfEmployment", target = "infoDateOfEmployment")
    @Mapping(source = "empl.baId", target = "infoEmplBa")
    @Mapping(source = "empl.positionId", target = "infoEmplPosition")
    public abstract SalaryRequestEntry toEntry(SalaryRequestReportBody body,
                                               EmployeeDetailedEntry empl,
                                               Integer createdBy, OffsetDateTime createdAt);

    @Mapping(source = "body.budgetExpectedFundingUntil", target = "budgetExpectedFundingUntil")
    @Mapping(source = "body.currentSalaryAmount", target = "infoCurrentSalaryAmount")
    @Mapping(source = "body.plannedSalaryAmount", target = "reqPlannedSalaryAmount")
    @Mapping(source = "body.previousSalaryIncreaseDate", target = "infoPreviousSalaryIncreaseDate")
    @Mapping(source = "body.previousSalaryIncreaseText", target = "infoPreviousSalaryIncreaseText")
    @Mapping(source = "body.newPosition", target = "reqNewPosition")
    @Mapping(source = "body.assessmentId", target = "assessmentId")
    @Mapping(source = "body.comment", target = "reqComment")
    public abstract void applyRequestUpdateBody(@MappingTarget SalaryRequestEntry entry, SalaryRequestUpdateBody body);


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
    @Mapping(source = "infoPreviousSalaryIncreaseDate", target = "employeeInfo.previousSalaryIncreaseDate")
    @Mapping(source = "infoPreviousSalaryIncreaseText", target = "employeeInfo.previousSalaryIncreaseText")
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
    @Mapping(source = "implIncreaseText", target = "impl.increaseText")
    @Mapping(source = ".", target = "req.newPosition", qualifiedByName = "reqNewPosition")
    @Mapping(source = ".", target = "impl.newPosition", qualifiedByName = "implNewPosition")
    @Mapping(source = "approvals", target = "approvals", qualifiedByName = "approvalsFromJson")
    public abstract SalaryRequestDto fromEntry(SalaryRequestView entry);

    @Mapping(source = ".", target = "employeeCurrentProject", qualifiedByName = "employeeCurrentProject")
    @Mapping(source = ".", target = "employeeBusinessAccount", qualifiedByName = "employeeBusinessAccount")
    public abstract EmployeeWithLatestSalaryRequestDto fromEntry(EmployeeWithLatestSalaryRequestView entry);

    @Mapping(source = "req.increaseAmount", target = "reqIncreaseAmount")
    @Mapping(source = "req.plannedSalaryAmount", target = "reqPlannedSalaryAmount")
    @Mapping(source = "req.reason", target = "reqReason")
    @Mapping(source = "req.newPosition.name", target = "reqNewPosition")
    @Mapping(source = "impl.increaseAmount", target = "implIncreaseAmount")
    @Mapping(source = "impl.salaryAmount", target = "implSalaryAmount")
    @Mapping(source = "impl.increaseStartPeriod", target = "implIncreaseStartPeriod", qualifiedByName = "period")
    @Mapping(source = "impl.rejectReason", target = "implRejectReason")
    @Mapping(source = "impl.increaseText", target = "implIncreaseText")
    @Mapping(source = "impl.state", target = "implState")
    @Mapping(source = "impl.newPosition.name", target = "implNewPosition")
    @Mapping(source = "impl.implementedBy.name", target = "implemented")
    @Mapping(source = "employee.name", target = "employee")
    @Mapping(source = "employeeInfo.currentProject.name", target = "employeeProject")
    @Mapping(source = "employeeInfo.currentProject.role", target = "employeeProjectRole")
    @Mapping(source = "employeeInfo.ba.name", target = "employeeBusinessAccount")
    @Mapping(source = "employeeInfo.currentSalaryAmount", target = "currentSalaryAmount")
    @Mapping(source = "employeeInfo.previousSalaryIncreaseDate", target = "previousSalaryIncreaseDate")
    @Mapping(source = "employeeInfo.previousSalaryIncreaseText", target = "previousSalaryIncreaseText")
    @Mapping(source = "employeeInfo.dateOfEmployment", target = "dateOfEmployment")
    @Mapping(source = "createdBy.name", target = "createdBy")
    @Mapping(source = "employeeInfo.position.name", target = "employeePosition")
    @Mapping(source = "budgetBusinessAccount.name", target = "budgetBusinessAccount")
    @Mapping(source = "assessment.name", target = "assessment")
    @Mapping(source = "type", target = "typeValue")
    public abstract SalaryRequestExportDto toExportDto(SalaryRequestDto dto);


    @AfterMapping
    protected void dtoAfterMapping(@MappingTarget SalaryRequestDto dto) {
        if (dto.getImpl() != null && dto.getImpl().getImplementedAt() == null) {
            dto.setImpl(null);
        }
    }

    public abstract SalaryRequestClosedPeriodDto closedPeriodFromEntry(SalaryRequestClosedPeriodEntry salaryRequestClosedPeriodEntry);

    @Mapping(source = "body.increaseAmount", target = "implIncreaseAmount")
    @Mapping(source = "body.salaryAmount", target = "implSalaryAmount")
    @Mapping(source = "body.increaseStartPeriod", target = "implIncreaseStartPeriod")
    @Mapping(source = "body.newPosition", target = "implNewPosition")
    @Mapping(source = "body.comment", target = "implComment")
    @Mapping(source = "implementedAt", target = "implementedAt")
    @Mapping(source = "implementedBy", target = "implementedBy")
    @Mapping(constant = "1", target = "implState")
    public abstract void applyRequestImplementBody(@MappingTarget SalaryRequestEntry entry, SalaryRequestImplementBody body, OffsetDateTime implementedAt, Integer implementedBy);

    @Mapping(source = "body.increaseText", target = "implIncreaseText")
    public abstract void applyUpdateImplIncreaseTextBody(@MappingTarget SalaryRequestEntry entry, SalaryRequestUpdateImplIncreaseTextBody body, OffsetDateTime implementedAt, Integer implementedBy);

    @Mapping(source = "body.reason", target = "implRejectReason")
    @Mapping(source = "body.comment", target = "implComment")
    @Mapping(source = "implementedAt", target = "implementedAt")
    @Mapping(source = "implementedBy", target = "implementedBy")
    @Mapping(constant = "2", target = "implState")
    public abstract void applyRequestRejectBody(@MappingTarget SalaryRequestEntry entry, SalaryRequestRejectBody body, OffsetDateTime implementedAt, Integer implementedBy);

    @Mapping(target = "createdBy", source = ".", qualifiedByName = "approvalCreatedBy")
    public abstract SalaryRequestApprovalDto fromEntry(SalaryRequestApprovalView entry);


// <editor-fold desc="named helpers">

    @Named("employee")
    protected SimpleDictDto employee(SalaryRequestView entry) {
        return simpleDto(entry.getEmployeeId(), entry.getEmployeeDisplayName());
    }

    @Named("budgetBusinessAccount")
    protected SimpleDictDto budgetBusinessAccount(SalaryRequestView entry) {
        return simpleDto(entry.getBudgetBusinessAccount(), entry.getBudgetBusinessAccountName());
    }

    @Named("employeeBusinessAccount")
    protected SimpleDictDto employeeBusinessAccount(SalaryRequestView entry) {
        return simpleDto(entry.getInfoEmplBa(), entry.getInfoEmplBaName());
    }


    @Named("assessment")
    protected SimpleDictDto assessment(SalaryRequestView entry) {
        return simpleDto(entry.getAssessmentId(), i18Helper.formatDate(entry.getAssessmentPlannedDate()));
    }


    @Named("employeePosition")
    protected SimpleDictDto employeePosition(SalaryRequestView entry) {
        return simpleDto(entry.getInfoEmplPosition(), entry.getInfoEmplPositionName());
    }

    @Named("implNewPosition")
    protected SimpleDictDto implNewPosition(SalaryRequestView entry) {
        return simpleDto(entry.getImplNewPosition(), entry.getImplNewPositionName());
    }

    @Named("reqNewPosition")
    protected SimpleDictDto reqNewPosition(SalaryRequestView entry) {
        return simpleDto(entry.getReqNewPosition(), entry.getReqNewPositionName());
    }

    @Named("createdBy")
    protected SimpleDictDto createdBy(SalaryRequestView entry) {
        return simpleDto(entry.getCreatedBy(), entry.getCreatedByDisplayName());
    }

    @Named("approvalCreatedBy")
    protected SimpleDictDto approvalCreatedBy(SalaryRequestApprovalView entry) {
        return simpleDto(entry.getCreatedBy(), entry.getCreatedByDisplayName());
    }


    @Named("implementedBy")
    protected SimpleDictDto implementedBy(SalaryRequestView entry) {
        return simpleDto(entry.getImplementedBy(), entry.getImplementedByDisplayName());
    }

    @Named("employeeCurrentProject")
    protected CurrentProjectDictDto currentProject(SalaryRequestView entry) {
        return currentProjectDto(entry.getInfoEmplProject(), entry.getInfoEmplProjectName(), entry.getInfoEmplProjectRole());
    }

    @Named("period")
    protected YearMonth period(Integer periodId) {
        return MapperBase.fromPeriodId(periodId);
    }

    @Named("approvalsFromJson")
    protected List<SalaryRequestApprovalDto> approvalsFromJson(Json json) {
        return listFromJson(json, SalaryRequestApprovalDto.class, Comparator
                .comparing(SalaryRequestApprovalDto::getCreatedAt).reversed());
    }

    @Named("employeeBusinessAccount")
    protected SimpleDictDto employeeBusinessAccount(EmployeeWithLatestSalaryRequestView entry) {
        return simpleDto(entry.getEmployeeBusinessAccountId(), entry.getEmployeeBusinessAccountName());
    }

    @Named("employeeCurrentProject")
    protected CurrentProjectDictDto employeeCurrentProject(EmployeeWithLatestSalaryRequestView entry) {
        return currentProjectDto(entry.getEmployeeCurrentProjectId(), entry.getEmployeeCurrentProjectName(), entry.getEmployeeCurrentProjectRole());
    }


// </editor-fold>


}
