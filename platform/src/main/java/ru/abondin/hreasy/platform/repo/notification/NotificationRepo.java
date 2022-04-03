package ru.abondin.hreasy.platform.repo.notification;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface NotificationRepo extends ReactiveCrudRepository<NotificationEntry, Integer> {


    Flux<NotificationEntry> findByEmployee(int employeeId);
}

