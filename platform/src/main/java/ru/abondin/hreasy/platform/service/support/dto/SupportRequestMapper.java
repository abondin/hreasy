package ru.abondin.hreasy.platform.service.support.dto;

import io.r2dbc.postgresql.codec.Json;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.abondin.hreasy.platform.repo.suport.SupportRequestEntry;
import ru.abondin.hreasy.platform.repo.suport.SupportRequestGroupEntry;
import ru.abondin.hreasy.platform.service.mapper.MapperBaseWithJsonSupport;

import java.time.OffsetDateTime;

@Mapper(componentModel = "spring")
public abstract class SupportRequestMapper extends MapperBaseWithJsonSupport {
    @Mapping(source = "employeeId", target = "createdBy")
    @Mapping(source = "employeeId", target = "employeeId")
    @Mapping(source = "now", target = "createdAt")
    @Mapping(source = "sourceType", target = "sourceType")
    @Mapping(source = "requestDto.group", target = "supportGroup")
    @Mapping(source = "requestDto.message", target = "message")
    @Mapping(source = "requestDto.category", target = "category")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    public abstract SupportRequestEntry fromNewRequest(int employeeId, int sourceType, OffsetDateTime now, NewSupportRequestDto requestDto);

    @Mapping(target = "configuration", source = "configuration", qualifiedByName = "groupConfiguration")
    public abstract SupportRequestGroupDto fromEntry(SupportRequestGroupEntry entry);

    @Named("groupConfiguration")
    public SupportGroupConfiguration groupConfiguration(Json json) {
        return fromJson(json, SupportGroupConfiguration.class);
    }
}
