package ru.abondin.hreasy.platform.service.ts.dto;

import io.r2dbc.postgresql.codec.Json;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import ru.abondin.hreasy.platform.repo.ts.TimesheetRecordEntry;
import ru.abondin.hreasy.platform.repo.ts.TimesheetSummaryView;
import ru.abondin.hreasy.platform.service.mapper.MapperBaseWithJsonSupport;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public abstract class TimesheetMapper extends MapperBaseWithJsonSupport {

    @Mapping(source = "entry.", target = "employee", qualifiedByName = "employee")
    @Mapping(source = "entry.timesheet", target = "timesheet", qualifiedByName = "timesheetJson")
    @Mapping(source = "entry.vacations", target = "vacationDays", qualifiedByName = "vacationDays")
    @Mapping(source = "businessAccount", target = "businessAccount")
    @Mapping(source = "project", target = "project")
    public abstract TimesheetSummaryDto fromView(TimesheetSummaryView entry, Integer businessAccount, Integer project);

    @Named("employee")
    protected TimesheetSummaryDto.EmployeeShortForTimesheetSummary employee(TimesheetSummaryView entry) {
        return TimesheetSummaryDto.EmployeeShortForTimesheetSummary.builder()
                .id(entry.getEmployeeId())
                .currentProject(entry.getEmployeeCurrentProject())
                .currentProjectBa(entry.getEmployeeBa())
                .displayName(entry.getEmployeeDisplayName())
                .build();
    }

    @Named("vacationDays")
    protected Set<LocalDate> vacationDays(Json data) {
        //TODO
        return new HashSet<>();
    }

    @Named("timesheetJson")
    protected List<TimesheetSummaryDto.TimesheetShortForSummary> timesheetJson(Json data) {
        return listFromJson(data, TimesheetSummaryDto.TimesheetShortForSummary.class);
    }

    public abstract TimesheetDto fromEntry(TimesheetRecordEntry entry);

    @Mapping(source = "employeeId", target = "employee")
    @Mapping(source = "businessAccount", target = "businessAccount")
    @Mapping(source = "project", target = "project")
    public abstract TimesheetRecordEntry toBaseEntry(Integer employeeId,
                                                     int businessAccount,
                                                     Integer project);


    @Mapping(source = "hours.date", target = "date")
    @Mapping(source = "hours.hoursSpent", target = "hoursSpent")
    @Mapping(source = "now", target = "updatedAt")
    @Mapping(source = "updatedBy", target = "updatedBy")
    public abstract TimesheetRecordEntry applyChanges(@MappingTarget TimesheetRecordEntry entry,
                                                      TimesheetReportBody.TimesheetReportOneDay hours,
                                                      String comment,
                                                      OffsetDateTime now, Integer updatedBy);
}
