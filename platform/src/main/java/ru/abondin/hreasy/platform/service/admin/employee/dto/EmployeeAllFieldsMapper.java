package ru.abondin.hreasy.platform.service.admin.employee.dto;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import ru.abondin.hreasy.platform.repo.employee.admin.EmployeeHistoryEntry;
import ru.abondin.hreasy.platform.repo.employee.admin.EmployeeWithAllDetailsEntry;
import ru.abondin.hreasy.platform.repo.employee.admin.EmployeeWithAllDetailsWithBaView;
import ru.abondin.hreasy.platform.repo.employee.admin.kids.EmployeeKidView;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;
import ru.abondin.hreasy.platform.service.mapper.MapperBase;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper(componentModel = "spring")
public interface EmployeeAllFieldsMapper extends MapperBase {


    @Mapping(target = "id", ignore = true)
    void populateFromBody(@MappingTarget EmployeeWithAllDetailsEntry entry, CreateOrUpdateEmployeeBody dto);

    default EmployeeHistoryEntry history(EmployeeWithAllDetailsEntry persisted, Integer createdBy, OffsetDateTime createdAt) {
        var history = historyBase(persisted);
        history.setCreatedAt(createdAt);
        history.setCreatedBy(createdBy);
        return history;
    }

    @Mapping(source = "id", target = "employee")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    EmployeeHistoryEntry historyBase(EmployeeWithAllDetailsEntry persisted);


    /**
     * Do not maps active field. Please use fromEntry method
     *
     * @param entry
     * @return
     */
    @Mapping(target = "documentFull", source = ".", qualifiedByName = "documentFull")
    //TODO Set phone datatype in database to string
    EmployeeWithAllDetailsDto fromEntryPartially(EmployeeWithAllDetailsEntry entry);


    @Mapping(target = "position", ignore = true)
    @Mapping(target = "level", ignore = true)
    @Mapping(target = "officeLocation", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "currentProject", ignore = true)
    @Mapping(target = "ba", ignore = true)
    EmployeeExportDto toExportWithoutDictionaries(EmployeeWithAllDetailsDto dto);

    default EmployeeWithAllDetailsDto fromEntry(EmployeeWithAllDetailsEntry entry, OffsetDateTime now) {
        var result = fromEntryPartially(entry);
        /**
         * Set employee active if dismissal date is not set or set to the future
         */
        result.setActive(entry.getDateOfDismissal() == null ||
                entry.getDateOfDismissal().isAfter(now.toLocalDate()));
        return result;
    }

    default EmployeeWithAllDetailsDto fromView(EmployeeWithAllDetailsWithBaView entry, OffsetDateTime now) {
        var result = fromEntry(entry, now);
        result.setBaId(entry.getBaId());
        return result;
    }

    /**
     * Do not forget to calculate age manually
     *
     * @param m
     * @return
     */
    @Mapping(target = "parent", source = ".", qualifiedByName = "kidParent")
    EmployeeKidDto fromEntry(EmployeeKidView m);


    @Named("kidParent")
    default SimpleDictDto kidParent(EmployeeKidView entry) {
        if (entry == null || entry.getParent() == null) {
            return null;
        }
        var result = new SimpleDictDto();
        result.setId(entry.getParent());
        result.setActive(entry.isParentNotDismissed());
        result.setName(entry.getParentDisplayName());
        return result;
    }

    @Named("documentFull")
    default String documentFull(EmployeeWithAllDetailsEntry entry) {
        return entry == null ? null : Stream.of(
                        entry.getDocumentSeries(),
                        entry.getDocumentNumber(),
                        entry.getDocumentIssuedBy(),
                        Optional.ofNullable(entry.getDocumentIssuedDate()).map(d -> d.format(DATE_FORMATTER)).orElse(null))
                .filter(s -> StringUtils.isNotBlank(s))
                .collect(Collectors.joining(" "));
    }


}
