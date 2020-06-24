package ru.abondin.hreasy.platform.service.mapper;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.abondin.hreasy.platform.repo.vacation.VacationView;
import ru.abondin.hreasy.platform.service.dto.VacationDto;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Map all dictionaries database entries to API DTO
 */
@Mapper(componentModel = "spring")
public interface VacationDtoMapper {

    @Mapping(target = "employeeDisplayName", source = ".", qualifiedByName = "toDisplayName")
    VacationDto vacationToDto(VacationView entry);

    @Named("toDisplayName")
    default String toDisplayName(VacationView entry) {
        return entry == null ? null : Stream.of(
                entry.getEmployeeLastname(),
                entry.getEmployeeFirstname(),
                entry.getEmployeePatronymicName())
                .filter(s -> StringUtils.isNotBlank(s))
                .collect(Collectors.joining(" "));
    }

}
