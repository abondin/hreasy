package ru.abondin.hreasy.platform.service.overtime.dto;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.abondin.hreasy.platform.repo.overtime.OvertimeApprovalDecisionEntry;
import ru.abondin.hreasy.platform.repo.overtime.OvertimeItemEntry;
import ru.abondin.hreasy.platform.repo.overtime.OvertimeItemView;
import ru.abondin.hreasy.platform.repo.overtime.OvertimeReportEntry;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper(componentModel = "spring")
public interface OvertimeMapper {

    @Mapping(target = "items", ignore = true)
    OvertimeReportDto reportToDto(OvertimeReportEntry entry);

    OvertimeItemDto itemToDto(OvertimeItemEntry entry);

    OvertimeReportEntry reportToEntry(OvertimeReportDto dto);

    @Mapping(target = "reportId", ignore = true)
    @Mapping(target = "createdEmployeeId", ignore = true)
    @Mapping(target = "deletedEmployeeId", ignore = true)
    OvertimeItemEntry itemToEntry(OvertimeItemDto dto);

    @Mapping(target = "reportId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdEmployeeId", ignore = true)
    @Mapping(target = "deletedEmployeeId", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    OvertimeItemEntry itemToEntry(NewOvertimeItemDto dto);

    OvertimeEmployeeSummary.OvertimeDaySummary viewToDto(OvertimeItemView overtimeItemView);

    @Mapping(target = "approverDisplayName", source = ".", qualifiedByName = "toDisplayName")
    OvertimeApprovalDecisionDto approvalToDto(OvertimeApprovalDecisionEntry.OvertimeApprovalDecisionWithEmployeeEntry entry);

    @Named("toDisplayName")
    default String toDisplayName(OvertimeApprovalDecisionEntry.OvertimeApprovalDecisionWithEmployeeEntry entry) {
        return entry == null ? null : Stream.of(
                entry.getApproverLastName(),
                entry.getApproverFirstName(),
                entry.getApproverPatronymicName())
                .filter(s -> StringUtils.isNotBlank(s))
                .collect(Collectors.joining(" "));
    }

}
