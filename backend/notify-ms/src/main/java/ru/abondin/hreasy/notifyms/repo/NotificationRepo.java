package ru.abondin.hreasy.notifyms.repo;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@Repository
public interface NotificationRepo extends ReactiveCrudRepository<NotificationEntry, Long> {
    Mono<NotificationEntry> findByDedupeKey(String dedupeKey);

    @Query("""
            delete
              from notify_ms.notification
             where created_at < :cutoff
         returning id
            """)
    Flux<Long> deleteCreatedBefore(OffsetDateTime cutoff);
}
