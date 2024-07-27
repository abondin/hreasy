package ru.abondin.hreasy.platform.service.udr.dto;

import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.abondin.hreasy.platform.repo.udr.UdrEntry;
import ru.abondin.hreasy.platform.service.mapper.MapperBaseWithJsonSupport;

import java.util.List;

@Mapper(componentModel = "spring")
@Slf4j
public abstract class UdrMapper extends MapperBaseWithJsonSupport {
    @Mapping(target = "customFields", source = ".", qualifiedByName = "customFields")
    public abstract JuniorDto toDto(UdrEntry udr);

    @Named("customFields")
    protected List<UdrCustomFieldConf> customFields(UdrEntry entry) {
        return listFromJson(entry.getCustomFields(), UdrCustomFieldConf.class);
    }
}
