package ru.abondin.hreasy.platform.service.project.dto;

import io.r2dbc.postgresql.codec.Json;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.abondin.hreasy.platform.repo.dict.DictProjectEntry;
import ru.abondin.hreasy.platform.service.dto.ManagerInfoDto;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;
import ru.abondin.hreasy.platform.service.mapper.MapperBaseWithJsonSupport;

import java.util.List;

@Mapper(componentModel = "spring")
@Slf4j
public abstract class ProjectCardMapper extends MapperBaseWithJsonSupport  {

    @Mapping(target = "department", qualifiedByName = "department", source = ".")
    @Mapping(target = "businessAccount", qualifiedByName = "businessAccount", source = ".")
    @Mapping(target = "managers", qualifiedByName = "managersJson", source = "managersJson")
    @Mapping(target = "baManagers", qualifiedByName = "managersJson", source = "baManagersJson")
    public abstract ProjectCardInfoDto fromEntry(DictProjectEntry.ProjectFullEntryWithManagers projectFullEntryWithManagers);

    @Named("managersJson")
    protected List<ManagerInfoDto> managersJson(Json json) {
        return listFromJson(json, ManagerInfoDto.class);
    }

    @Named("department")
    protected SimpleDictDto department(DictProjectEntry.ProjectFullEntryWithManagers entry) {
        return simpleDto(entry.getDepartmentId(), entry.getDepartmentName());
    }

    @Named("businessAccount")
    protected SimpleDictDto businessAccount(DictProjectEntry.ProjectFullEntryWithManagers entry) {
        return simpleDto(entry.getBaId(), entry.getBaName());
    }
}
