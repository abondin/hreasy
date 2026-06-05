package ru.abondin.hreasy.platform.repo.udr;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.r2dbc.postgresql.codec.Json;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import ru.abondin.hreasy.platform.service.PostgresJSONJacksonSerializer;

import java.time.OffsetDateTime;

@Data
@Table(value = "junior_report", schema = "udr")
public class JuniorReportEntry {
    @Id
    private Integer id;

    private Integer juniorId;

    private short progress;

    private String comment;

    private OffsetDateTime createdAt;
    private Integer createdBy;

    private OffsetDateTime deletedAt;
    private Integer deletedBy;

    @JsonSerialize(using = PostgresJSONJacksonSerializer.class)
    private Json ratings;
}