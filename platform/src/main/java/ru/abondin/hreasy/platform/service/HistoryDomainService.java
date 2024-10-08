package ru.abondin.hreasy.platform.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.codec.Json;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.repo.history.HistoryEntry;
import ru.abondin.hreasy.platform.repo.history.HistoryRepo;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class HistoryDomainService {

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public enum HistoryEntityType {
        OFFICE("office"),
        OFFICE_LOCATION("office_location"),
        MANAGER("empl_manager"),
        TIMESHEET_RECORD("timesheet_record"),
        SALARY_REQUEST("salary_request"),
        SALARY_REQUEST_APPROVAL("salary_request_approval"),
        SALARY_REQUEST_CLOSED_REPORT_PERIOD("salary_request_closed_report_period"),
        SUPPORT_REQUEST("support_request"),
        JUNIOR_REGISTRY("junior_registry"),
        JUNIOR_REGISTRY_REPORT("junior_registry_report");
        private final String type;
    }

    private final ObjectMapper objectMapper;
    private final HistoryRepo repo;


    public <T> Mono<HistoryEntry> persistHistory(int entityId, HistoryEntityType entityType, T entity,
                                                 OffsetDateTime createdAt, int createdBy) {
        try {
            var content = entity == null ? null : Json.of(objectMapper.writeValueAsString(entity));
            var history = new HistoryEntry();
            history.setCreatedAt(createdAt);
            history.setCreatedBy(createdBy);
            history.setEntityId(entityId);
            history.setEntityType(entityType.getType());
            history.setEntityValue(content);
            return repo.save(history);
        } catch (JsonProcessingException e) {
            log.error("Unable to serialize entity for {}:{}", entityType, entityId);
            return Mono.error(new BusinessError("unexpected.history.serialization.error",
                    entityType.getType(), Integer.toString(entityId)));
        }
    }
}
