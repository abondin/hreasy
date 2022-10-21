package ru.abondin.hreasy.platform.service.dict.dto;

import io.r2dbc.postgresql.codec.Json;
import org.mapstruct.Mapper;
import ru.abondin.hreasy.platform.service.mapper.MapperBaseWithJsonSupport;

import java.util.List;


@Mapper(componentModel = "spring")
public class DictWorkingDaysCalendarMapper extends MapperBaseWithJsonSupport {
    public List<DictWorkingDaysCalendarDto> calendarFromJson(Json calendar) {
        return listFromJson(calendar, DictWorkingDaysCalendarDto.class);
    }

}
