package ru.abondin.hreasy.platform.service.overtime.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.abondin.hreasy.platform.repo.overtime.OvertimeItemEntry;
import ru.abondin.hreasy.platform.repo.overtime.OvertimeReportEntry;

@Mapper(componentModel = "spring")
public interface OvertimeMapper {

    OvertimeReportDto reportToDto(OvertimeReportEntry entry);

    OvertimeItemDto itemToDto(OvertimeItemEntry entry);

    OvertimeReportEntry reportToEntry(OvertimeReportDto dto);

    OvertimeItemEntry itemToEntry(OvertimeItemDto dto);

    OvertimeItemEntry itemToEntry(NewOvertimeItemDto dto);

}
