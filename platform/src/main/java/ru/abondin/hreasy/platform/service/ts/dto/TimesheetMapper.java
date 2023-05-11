package ru.abondin.hreasy.platform.service.ts.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.abondin.hreasy.platform.repo.ts.TimesheetRecordEntry;
import ru.abondin.hreasy.platform.service.mapper.MapperBase;

import java.time.OffsetDateTime;

@Mapper(componentModel = "spring")
public interface TimesheetMapper extends MapperBase {

    TimesheetSummaryDto fromEntry(TimesheetRecordEntry entry);

    @Mapping(source = "employeeId", target = "employee")
    @Mapping(source = "businessAccount", target = "businessAccount")
    @Mapping(source = "project", target = "project")
    TimesheetRecordEntry toBaseEntry(Integer employeeId,
                                     int businessAccount,
                                     Integer project);


    @Mapping(source = "hours.date", target = "date")
    @Mapping(source = "hours.hoursSpent", target = "hoursSpent")
    @Mapping(source = "now", target = "updatedAt")
    @Mapping(source = "updatedBy", target = "updatedBy")
    TimesheetRecordEntry applyChanges(@MappingTarget TimesheetRecordEntry entry,
                      TimesheetReportBody.TimesheetReportOneDay hours,
                      String comment,
                      OffsetDateTime now, Integer updatedBy);
}
