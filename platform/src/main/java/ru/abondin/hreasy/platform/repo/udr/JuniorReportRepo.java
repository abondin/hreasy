package ru.abondin.hreasy.platform.repo.udr;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

public interface JuniorReportRepo extends ReactiveCrudRepository<JuniorReportEntry, Integer> {

    @Query("update udr.junior_report set deleted_by=:deletedBy, deleted_at=:deletedAt where junior_id=:juniorId")
    Mono<Integer> markAllAsDeleted(int juniorId, Integer deletedBy, OffsetDateTime deletedAt);
}
