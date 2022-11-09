package ru.abondin.hreasy.platform.service.mapper;

import org.mapstruct.*;
import ru.abondin.hreasy.platform.I18Helper;
import ru.abondin.hreasy.platform.repo.vacation.VacationEntry;
import ru.abondin.hreasy.platform.repo.vacation.VacationView;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;
import ru.abondin.hreasy.platform.service.vacation.dto.*;

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

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "vacationId", source = "id")
    @Mapping(target = "id", ignore = true)
    VacationEntry.VacationHistoryEntry history(VacationEntry v);

    @Mapping(target = "employeeCurrentProject", source = "employeeCurrentProject.name")
    @Mapping(target = "status", qualifiedByName = "vacationStatus")
    VacationExportDto toExportProjection(VacationDto vacation, @Context I18Helper helper, @Context Locale locale);

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
    default SimpleDictDto employeeCurrentProject(VacationView entry) {
        return simpleDto(entry.getEmployeeCurrentProject(), entry.getEmployeeCurrentProjectName());
    }


}
