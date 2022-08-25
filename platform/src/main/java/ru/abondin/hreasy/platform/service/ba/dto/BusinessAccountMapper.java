package ru.abondin.hreasy.platform.service.ba.dto;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.abondin.hreasy.platform.repo.ba.*;
import ru.abondin.hreasy.platform.service.admin.ba.dto.BusinessAccountPositionWithRateDto;
import ru.abondin.hreasy.platform.service.mapper.MapperBase;

@Mapper(componentModel = "spring")
public interface BusinessAccountMapper extends MapperBase {


    BusinessAccountDto fromEntry(BusinessAccountEntryView entry);


    BusinessAccountPositionWithRateDto toPositionWithRate(BusinessAccountPositionEntry entry);

    @Mapping(source = "id", target = "baId")
    @Mapping(target = "id", ignore = true)
    BusinessAccountHistoryEntry history(BusinessAccountEntry entry);

    @Mapping(source = "id", target = "baPositionId")
    @Mapping(target = "id", ignore = true)
    BusinessAccountPositionHistoryEntry history(BusinessAccountPositionEntry entry);

    @Mapping(source = "id", target = "baAssignmentId")
    @Mapping(target = "id", ignore = true)
    BusinessAccountAssignmentHistoryEntry history(BusinessAccountAssignmentEntry entry);


}
