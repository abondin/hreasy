package ru.abondin.hreasy.platform.service.ts.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.abondin.hreasy.platform.repo.ts.TimesheetRecordEntry;
import ru.abondin.hreasy.platform.repo.ts.TimesheetSummaryView;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;
import ru.abondin.hreasy.platform.service.mapper.MapperBase;

import java.time.OffsetDateTime;

@Mapper(componentModel = "spring")
public interface TimesheetMapper extends MapperBase {

    @Mapping(source = ".", target = "employee", qualifiedByName = "employee")
    TimesheetSummaryDto fromEntry(TimesheetSummaryView entry);

    @Mapping(source = "employeeId", target = "employee")
    @Mapping(source = "body.businessAccount", target = "businessAccount")
    @Mapping(source = "body.project", target = "project")
    @Mapping(source = "body.date", target = "date")
    @Mapping(source = "body.hours", target = "hours")
    @Mapping(source = "now", target = "createdAt")
    @Mapping(source = "createdBy", target = "createdBy")
    TimesheetRecordEntry toEntry(Integer employeeId, TimesheetReportBody body, OffsetDateTime now, Integer createdBy);


    @Named("employee")
    default SimpleDictDto employee(TimesheetSummaryView entry) {
        return simpleDto(entry.getEmployee(), entry.getEmployeeDisplayName());
    }
}
