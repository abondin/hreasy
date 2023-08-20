package ru.abondin.hreasy.platform.service.ts.dto;

import io.r2dbc.postgresql.codec.Json;
import lombok.Data;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import ru.abondin.hreasy.platform.repo.ts.TimesheetRecordEntry;
import ru.abondin.hreasy.platform.repo.ts.TimesheetSummaryView;
import ru.abondin.hreasy.platform.service.mapper.MapperBaseWithJsonSupport;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper(componentModel = "spring")
public abstract class TimesheetMapper extends MapperBaseWithJsonSupport {

    @Data
    public static class VacationDates {
        private LocalDate startDate;
        private LocalDate endDate;
    }

    @Mapping(source = ".", target = "employee", qualifiedByName = "employee")
    @Mapping(source = "timesheet", target = "timesheet", qualifiedByName = "timesheetJson")
    @Mapping(source = "vacations", target = "vacationDays", qualifiedByName = "vacationDays")
    public abstract TimesheetSummaryDto fromView(TimesheetSummaryView entry);

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
        return listFromJson(data, VacationDates.class)
                .stream().filter(v -> v.startDate != null && v.endDate != null && !v.endDate.isBefore(v.startDate))
                .flatMap(v -> Stream.iterate(v.getStartDate(), d -> d.plusDays(1))
                        .limit(ChronoUnit.DAYS.between(v.startDate, v.endDate) + 1))
                .collect(Collectors.toSet());
    }

    @Named("timesheetJson")
    protected List<TimesheetDto> timesheetJson(Json data) {
        return listFromJson(data, TimesheetDto.class);
    }

    public abstract TimesheetDto fromEntry(TimesheetRecordEntry entry);

    @Mapping(source = "employeeId", target = "employee")
    @Mapping(source = "businessAccount", target = "businessAccount")
    @Mapping(source = "project", target = "project")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "hoursSpent", ignore = true)
    @Mapping(target = "comment", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    public abstract TimesheetRecordEntry toBaseEntry(Integer employeeId,
                                                     int businessAccount,
                                                     Integer project);


    @Mapping(source = "hours.date", target = "date")
    @Mapping(source = "hours.hoursSpent", target = "hoursSpent")
    @Mapping(source = "now", target = "updatedAt")
    @Mapping(source = "updatedBy", target = "updatedBy")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "businessAccount", ignore = true)
    @Mapping(target = "project", ignore = true)
    public abstract TimesheetRecordEntry applyChanges(@MappingTarget TimesheetRecordEntry entry,
                                                      TimesheetReportBody.TimesheetReportOneDay hours,
                                                      String comment,
                                                      OffsetDateTime now, Integer updatedBy);
}
