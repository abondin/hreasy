package ru.abondin.hreasy.platform.service.overtime.dto;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.abondin.hreasy.platform.repo.overtime.*;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper(componentModel = "spring")
public interface OvertimeMapper {

    @Mapping(target = "items", ignore = true)
    @Mapping(target = "employeeId", source = "employee")
    OvertimeReportDto reportToDto(OvertimeReportEntry entry);

    OvertimeItemDto itemToDto(OvertimeItemEntry entry);


    @Mapping(target = "reportId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdEmployeeId", ignore = true)
    @Mapping(target = "deletedEmployeeId", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    OvertimeItemEntry itemToEntry(NewOvertimeItemDto dto);

    OvertimeEmployeeSummary.OvertimeDaySummary viewToDto(OvertimeItemsGroupedByDateAndProjectView overtimeItemView);

    @Mapping(source = "id", target = "reportId")
    OvertimeEmployeeSummary summaryFromEntry(OvertimeReportEntry.OvertimeReportSummaryEntry entry);

    @Mapping(target = "approverDisplayName", source = ".", qualifiedByName = "toDisplayName")
    OvertimeApprovalDecisionDto fromEntry(OvertimeApprovalDecisionEntry.OvertimeApprovalDecisionWithEmployeeEntry entry);

    OvertimeClosedPeriodDto fromEntry(OvertimeClosedPeriodEntry entry);

    @Named("toDisplayName")
    default String toDisplayName(OvertimeApprovalDecisionEntry.OvertimeApprovalDecisionWithEmployeeEntry entry) {
        return entry == null ? null : Stream.of(
                entry.getApproverLastName(),
                entry.getApproverFirstName(),
                entry.getApproverPatronymicName())
                .filter(s -> StringUtils.isNotBlank(s))
                .collect(Collectors.joining(" "));
    }

    default OvertimeApprovalDecisionDto approvalToDtoIncludeOutdated(OvertimeApprovalDecisionEntry.OvertimeApprovalDecisionWithEmployeeEntry approvalEntry,
                                                                     OvertimeReportDto report) {
        var dto = fromEntry(approvalEntry);
        var lastUpdate = report.getLastUpdate();
        dto.setOutdated(lastUpdate != null && dto.getDecisionTime().isBefore(lastUpdate));
        return dto;
    }
}
