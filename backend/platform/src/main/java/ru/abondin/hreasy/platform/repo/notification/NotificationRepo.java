package ru.abondin.hreasy.platform.repo.notification;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface NotificationRepo extends ReactiveCrudRepository<NotificationEntry, Integer> {

    @Query("""
            select *
              from notify.notification
             where employee = :employeeId
               and archived_at is null
             order by created_at desc, id desc
            """)
    Flux<NotificationEntry> findByEmployee(int employeeId);

    @Query("""
            select count(*)
              from notify.notification
             where employee = :employeeId
               and acknowledged_at is null
               and archived_at is null
            """)
    Mono<Integer> countUnreadByEmployee(int employeeId);

    @Query("""
            update notify.notification
               set acknowledged_at = :now, acknowledged_by = :employeeId
             where employee = :employeeId and id in (:ids)
               and acknowledged_at is null and archived_at is null
         returning id
            """)
    Flux<Integer> acknowledgeByEmployee(int employeeId, List<Integer> ids, OffsetDateTime now);

    @Query("""
            delete
              from notify.notification
             where created_at < :cutoff
         returning id
            """)
    Flux<Integer> deleteCreatedBefore(OffsetDateTime cutoff);
}
