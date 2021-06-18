package ru.abondin.hreasy.platform.service.admin.employee.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.abondin.hreasy.platform.repo.employee.admin.EmployeeHistoryEntry;
import ru.abondin.hreasy.platform.repo.employee.admin.EmployeeWithAllDetailsEntry;

import java.time.OffsetDateTime;

@Mapper(componentModel = "spring")
public interface EmployeeAllFieldsMapper {

    void populateFromBody(@MappingTarget EmployeeWithAllDetailsEntry entry, CreateOrUpdateEmployeeBody dto);

    default EmployeeHistoryEntry history(EmployeeWithAllDetailsEntry persisted, Integer createdBy, OffsetDateTime createdAt) {
        var history = historyBase(persisted);
        history.setCreatedAt(createdAt);
        history.setCreatedBy(createdBy);
        return history;
    }

    @Mapping(source = "id", target = "employee")
    @Mapping(target = "id", ignore = true)
    EmployeeHistoryEntry historyBase(EmployeeWithAllDetailsEntry persisted);

    EmployeeWithAllDetailsDto fromEntry(EmployeeWithAllDetailsEntry entry);
}
