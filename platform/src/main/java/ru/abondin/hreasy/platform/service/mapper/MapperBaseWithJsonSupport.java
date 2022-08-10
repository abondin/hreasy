package ru.abondin.hreasy.platform.service.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.codec.Json;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import ru.abondin.hreasy.platform.BusinessError;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Help in JSON converting
 */
@Slf4j
public abstract class MapperBaseWithJsonSupport implements MapperBase {
    @Autowired
    protected ObjectMapper objectMapper;

    protected <T> List<T> readList(Json json, Class<T> type) {
        if (json == null) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readerForListOf(type).readValue(json.asString());
        } catch (IOException e) {
            log.error("Unable to parse JSON", e);
            throw new BusinessError("internal.serialization.error");
        }
    }
}
