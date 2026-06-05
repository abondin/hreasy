package ru.abondin.hreasy.notifyms.repo;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.OffsetDateTime;

@Repository
public interface NotificationDeliveryRepo extends ReactiveCrudRepository<NotificationDeliveryEntry, Long> {

    @Query("""
            update notify_ms.notification_delivery d
               set status = 'sending',
                   updated_at = :now
             where d.id in (
                   select id
                     from notify_ms.notification_delivery
                    where status in ('queued', 'deferred', 'retry_scheduled')
                      and due_at <= :now
                      and error_count < max_attempts
                    order by due_at asc, id asc
                    limit :limit
                    for update skip locked
             )
             returning *
            """)
    Flux<NotificationDeliveryEntry> claimDue(OffsetDateTime now, int limit);

    @Query("""
            delete
              from notify_ms.notification_delivery d
            where exists (
                   select 1
                     from notify_ms.notification n
                    where n.id = d.notification_id
                      and n.created_at < :cutoff
             )
         returning id
            """)
    Flux<Long> deleteForNotificationsCreatedBefore(OffsetDateTime cutoff);
}
