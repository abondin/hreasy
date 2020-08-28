package ru.abondin.hreasy.platform.service.overtime.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.abondin.hreasy.platform.repo.overtime.OvertimeItemEntry;
import ru.abondin.hreasy.platform.repo.overtime.OvertimeItemView;
import ru.abondin.hreasy.platform.repo.overtime.OvertimeReportEntry;

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
}
