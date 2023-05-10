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
    @Mapping(source = "businessAccount", target = "businessAccount")
    @Mapping(source = "project", target = "project")
    @Mapping(source = "hours.date", target = "date")
    @Mapping(source = "hours.hoursSpent", target = "hoursSpent")
    @Mapping(source = "now", target = "createdAt")
    @Mapping(source = "createdBy", target = "createdBy")
    TimesheetRecordEntry toEntry(Integer employeeId,
                                 int businessAccount,
                                 Integer project,
                                 String comment,
                                 TimesheetReportBody.TimesheetReportOneDay hours,
                                 OffsetDateTime now, Integer createdBy);

}
