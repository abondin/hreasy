package ru.abondin.hreasy.platform.service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.r2dbc.postgresql.codec.Json;
import ru.abondin.hreasy.platform.repo.udr.JuniorReportEntry;

import java.io.IOException;
import java.time.OffsetDateTime;

/**
 * Serialize JSON Postgres Type to escaped string.
 * Created to persist {@link JuniorReportEntry#getRatings()} history in {@link HistoryDomainService#persistHistory(int, HistoryDomainService.HistoryEntityType, Object, OffsetDateTime, int)}
 */
public class PostgresJSONJacksonSerializer extends JsonSerializer<Json> {

    @Override
    public void serialize(Json json, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(json.asString());
    }
}
