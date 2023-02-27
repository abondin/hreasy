package ru.abondin.hreasy.platform.service.ts.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.abondin.hreasy.platform.repo.ts.TimesheetRecordEntry;
import ru.abondin.hreasy.platform.service.mapper.MapperBase;

import java.time.OffsetDateTime;

@Mapper(componentModel = "spring")
public interface TimesheetMapper extends MapperBase {

    TimesheetSummaryDto fromEntry(TimesheetRecordEntry entry);

    @Mapping(source = "employeeId", target = "employee")
    @Mapping(source = "body.businessAccount", target = "businessAccount")
    @Mapping(source = "body.project", target = "project")
    @Mapping(source = "body.date", target = "date")
    @Mapping(source = "body.hoursSpent", target = "hoursSpent")
    @Mapping(source = "body.hoursPlanned", target = "hoursPlanned")
    @Mapping(source = "body.billable", target = "billable")
    @Mapping(source = "body.description", target = "description")
    @Mapping(source = "now", target = "createdAt")
    @Mapping(source = "createdBy", target = "createdBy")
    TimesheetRecordEntry toEntry(Integer employeeId, TimesheetReportBody body, OffsetDateTime now, Integer createdBy);

}
