package ru.abondin.hreasy.platform.service.ba.dto;


import io.r2dbc.postgresql.codec.Json;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.abondin.hreasy.platform.repo.ba.*;
import ru.abondin.hreasy.platform.service.admin.ba.dto.BusinessAccountPositionWithRateDto;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;
import ru.abondin.hreasy.platform.service.mapper.MapperBaseWithJsonSupport;

import java.util.List;

@Mapper(componentModel = "spring")
@Slf4j
public abstract class BusinessAccountMapper extends MapperBaseWithJsonSupport {


    @Mapping(target = "managers", qualifiedByName = "managersJson", source = "managersJson")
    public abstract BusinessAccountDto fromEntry(BusinessAccountEntryView entry);

    @Named("managersJson")
    protected List<SimpleDictDto> managersJson(Json json) {
        return listFromJson(json, j -> new SimpleDictDto(j.get("employeeId").asInt(), j.get("employeeName").asText()));
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
