package ru.abondin.hreasy.platform.service.ba.dto;


import io.r2dbc.postgresql.codec.Json;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.abondin.hreasy.platform.repo.ba.BusinessAccountEntry;
import ru.abondin.hreasy.platform.repo.ba.BusinessAccountEntryView;
import ru.abondin.hreasy.platform.repo.ba.BusinessAccountHistoryEntry;
import ru.abondin.hreasy.platform.service.dto.ManagerInfoDto;
import ru.abondin.hreasy.platform.service.mapper.MapperBaseWithJsonSupport;

import java.util.List;

@Mapper(componentModel = "spring")
@Slf4j
public abstract class BusinessAccountMapper extends MapperBaseWithJsonSupport {


    @Mapping(target = "managers", qualifiedByName = "managersJson", source = "managersJson")
    public abstract BusinessAccountDto fromEntry(BusinessAccountEntryView entry);

    @Named("managersJson")
    protected List<ManagerInfoDto> managersJson(Json json) {
        return listFromJson(json, ManagerInfoDto.class);
    }


    @Mapping(source = "id", target = "baId")
    @Mapping(target = "id", ignore = true)
    public abstract BusinessAccountHistoryEntry history(BusinessAccountEntry entry);
}
