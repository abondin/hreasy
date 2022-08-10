package ru.abondin.hreasy.platform.service.ba.dto;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.abondin.hreasy.platform.repo.ba.*;
import ru.abondin.hreasy.platform.service.admin.ba.dto.BusinessAccountPositionWithRateDto;
import ru.abondin.hreasy.platform.service.mapper.MapperBaseWithJsonSupport;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class BusinessAccountMapper extends MapperBaseWithJsonSupport {

    @Mapping(source = ".", target = "responsibleEmployees", qualifiedByName = "responsibleEmployees")
    public abstract BusinessAccountDto fromEntry(BusinessAccountEntry entry);

    @Named("responsibleEmployees")
    public List<BusinessAccountResponsibleEmployeeDto> responsibleEmployee(BusinessAccountEntry entry) {
        return readList(entry.getResponsibleEmployees(), BusinessAccountResponsibleEmployeeDto.class);
    }

    public abstract BusinessAccountPositionWithRateDto toPositionWithRate(BusinessAccountPositionEntry entry);

    @Mapping(source = "id", target = "baId")
    @Mapping(target = "id", ignore = true)
    public abstract BusinessAccountHistoryEntry history(BusinessAccountEntry entry);

    @Mapping(source = "id", target = "baPositionId")
    @Mapping(target = "id", ignore = true)
    public abstract BusinessAccountPositionHistoryEntry history(BusinessAccountPositionEntry entry);

    @Mapping(source = "id", target = "baAssignmentId")
    @Mapping(target = "id", ignore = true)
    public abstract BusinessAccountAssignmentHistoryEntry history(BusinessAccountAssignmentEntry entry);

}
