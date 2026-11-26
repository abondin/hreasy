package ru.abondin.hreasy.platform.service;

import io.r2dbc.postgresql.codec.Json;
import ru.abondin.hreasy.platform.repo.udr.JuniorReportEntry;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.time.OffsetDateTime;

/**
 * Serialize JSON Postgres Type to escaped string.
 * Created to persist {@link JuniorReportEntry#getRatings()} history in {@link HistoryDomainService#persistHistory(int, HistoryDomainService.HistoryEntityType, Object, OffsetDateTime, int)}
 */
public class PostgresJSONJacksonSerializer extends ValueSerializer<Json> {

    @Override
    public void serialize(Json json, JsonGenerator gen, SerializationContext serializers) {
        gen.writeString(json.asString());
    }
}
