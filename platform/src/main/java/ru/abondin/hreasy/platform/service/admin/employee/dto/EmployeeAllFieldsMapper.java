package ru.abondin.hreasy.platform.service.admin.employee.dto;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import ru.abondin.hreasy.platform.repo.employee.admin.EmployeeHistoryEntry;
import ru.abondin.hreasy.platform.repo.employee.admin.EmployeeWithAllDetailsEntry;
import ru.abondin.hreasy.platform.service.mapper.MapperBase;

import java.math.BigDecimal;
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
    @Mapping(target = "displayName", source = ".", qualifiedByName = "displayName")
    @Mapping(target = "documentFull", source = ".", qualifiedByName = "documentFull")
    @Mapping(target = "phone", source = "phone", qualifiedByName = "phoneToString")
    //TODO Set phone datatype in database to string
    EmployeeWithAllDetailsDto fromEntryPartially(EmployeeWithAllDetailsEntry entry);

    default EmployeeWithAllDetailsDto fromEntry(EmployeeWithAllDetailsEntry entry, OffsetDateTime now) {
        var result = fromEntryPartially(entry);
        /**
         * Set employee active if dismissal date is not set or set to the future
         */
        result.setActive(entry.getDateOfDismissal() == null ||
                entry.getDateOfDismissal().isAfter(now.toLocalDate()));
        return result;
    }

    //TODO Set phone datatype in database to string
    @Deprecated
    default String phoneToString(BigDecimal phone) {
        return phone == null ? null : phone.toPlainString();
    }

    @Named("displayName")
    default String displayName(EmployeeWithAllDetailsEntry entry) {
        return entry == null ? null : Stream.of(
                entry.getLastname(),
                entry.getFirstname(),
                entry.getPatronymicName())
                .filter(s -> StringUtils.isNotBlank(s))
                .collect(Collectors.joining(" "));
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
