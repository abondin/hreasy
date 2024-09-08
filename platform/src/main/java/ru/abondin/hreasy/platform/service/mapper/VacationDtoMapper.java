package ru.abondin.hreasy.platform.service.mapper;

import org.mapstruct.*;
import ru.abondin.hreasy.platform.I18Helper;
import ru.abondin.hreasy.platform.repo.vacation.VacPlanningPeriodEntry;
import ru.abondin.hreasy.platform.repo.vacation.VacationEntry;
import ru.abondin.hreasy.platform.repo.vacation.VacationView;
import ru.abondin.hreasy.platform.service.dto.CurrentProjectDictDto;
import ru.abondin.hreasy.platform.service.vacation.dto.*;

import java.time.OffsetDateTime;
import java.util.Locale;

/**
 * Map all dictionaries database entries to API DTO
 */
@Mapper(componentModel = "spring")
public interface VacationDtoMapper extends MapperBase {

    @Mapping(target = "status", source = "status", qualifiedByName = "vacationStatusFromId")
    @Mapping(target = "employeeCurrentProject", qualifiedByName = "employeeCurrentProject", source = ".")
    VacationDto vacationToDto(VacationView entry);

    @Mapping(target = "status", source = "status", qualifiedByName = "vacationStatusFromId")
    MyVacationDto toMyDto(VacationEntry e);

    EmployeeVacationShort toEmployeeVacationShortDto(VacationEntry e);

    @Mapping(target = "status", qualifiedByName = "vacationStatusToId", source = "status")
    VacationEntry copyToEntry(VacationCreateOrUpdateDto body, @MappingTarget VacationEntry entry);

    default VacationEntry toEntry(VacationCreateOrUpdateDto body) {
        var entry = new VacationEntry();
        copyToEntry(body, entry);
        return entry;
    }

    @Mapping(target = "status", expression = "java(VacationDto.VacationStatus.REQUESTED.getStatusId())")
    @Mapping(target = "year", source = "request.year")
    @Mapping(target = "startDate", source = "request.startDate")
    @Mapping(target = "endDate", source = "request.endDate")
    @Mapping(target = "daysNumber", source = "request.daysNumber")
    @Mapping(target = "notes", source = "request.notes")
    @Mapping(target = "employee", source = "employeeId")
    @Mapping(target = "createdBy", source = "employeeId")
    @Mapping(target = "createdAt", source = "now")
    VacationEntry toEntry(MyPlannedVacationRequestDto request, int employeeId, OffsetDateTime now);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "vacationId", source = "id")
    @Mapping(target = "id", ignore = true)
    VacationEntry.VacationHistoryEntry history(VacationEntry v);

    @Mapping(target = "employeeCurrentProject", source = "employeeCurrentProject.name")
    @Mapping(target = "employeeCurrentProjectRole", source = "employeeCurrentProject.role")
    @Mapping(target = "status", qualifiedByName = "vacationStatus")
    VacationExportDto toExportProjection(VacationDto vacation, @Context I18Helper helper, @Context Locale locale);

    VacPlanningPeriodDto fromEntry(VacPlanningPeriodEntry entry);

    @Named("vacationStatus")
    default String vacationStatus(VacationDto.VacationStatus status, @Context I18Helper helper, @Context Locale locale) {
        return helper.localize(locale, "enum.VacationStatus." + status.toString());
    }


    @Named("vacationStatusFromId")
    default VacationDto.VacationStatus vacationStatusFromId(int statusId) {
        return VacationDto.VacationStatus.fromId(statusId);
    }

    @Named("vacationStatusToId")
    default int vacationStatusToId(VacationDto.VacationStatus status) {
        return status.getStatusId();
    }

    @Named("employeeCurrentProject")
    default CurrentProjectDictDto employeeCurrentProject(VacationView entry) {
        return currentProjectDto(entry.getEmployeeCurrentProject(), entry.getEmployeeCurrentProjectName(), entry.getEmployeeCurrentProjectRole());
    }


}
