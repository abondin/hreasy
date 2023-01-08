package ru.abondin.hreasy.platform.service.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.codec.Json;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import ru.abondin.hreasy.platform.BusinessError;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapper with Jakson JSON serialization support
 */
@Slf4j
public abstract class MapperBaseWithJsonSupport implements MapperBase{
    @Autowired
    protected ObjectMapper jsonMapper;

    protected <T> List<T> listFromJson(Json json, Class<T> type) {
        if (json == null) {
            return new ArrayList<>();
        }
        try {
            return jsonMapper.readerForListOf(type).readValue(json.asString());
        } catch (IOException e) {
            log.error("Unable to serialize json", e);
            throw new BusinessError("error.json.serialization");
        }
    }

    protected <T> T fromJson(Json json, Class<T> type){
        try {
            return json == null ? null : jsonMapper.readValue(json.asString(), type);
        } catch (JsonProcessingException e) {
            log.error("Unable to serialize json", e);
            throw new BusinessError("error.json.serialization");
        }
    }

    protected <T> String toJsonString(T instance){
        try {
            return instance == null ? null : jsonMapper.writeValueAsString(instance);
        } catch (JsonProcessingException e) {
            log.error("Unable to serialize json", e);
            throw new BusinessError("error.json.serialization");
        }
    }
}
