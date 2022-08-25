package ru.abondin.hreasy.platform.service.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.r2dbc.postgresql.codec.Json;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import ru.abondin.hreasy.platform.BusinessError;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Mapper with Jakson JSON serialization support
 */
@Slf4j
public abstract class MapperBaseWithJsonSupport {
    @Autowired
    protected ObjectMapper jsonMapper;

    protected <T> List<T> listFromJson(Json json, Function<ObjectNode, T> mapper) {
        if (json == null){
            return new ArrayList<>();
        }
        try {
            List<ObjectNode> nodes = jsonMapper.readerForListOf(ObjectNode.class).readValue(json.asString());
            return nodes.stream().map(mapper)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Unable to serialize json", e);
            throw new BusinessError("error.json.serialization");
        }
    }
}
