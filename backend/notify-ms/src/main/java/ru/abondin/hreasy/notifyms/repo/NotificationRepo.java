package ru.abondin.hreasy.notifyms.repo;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface NotificationRepo extends ReactiveCrudRepository<NotificationEntry, Long> {
    Mono<NotificationEntry> findByDedupeKey(String dedupeKey);
}
