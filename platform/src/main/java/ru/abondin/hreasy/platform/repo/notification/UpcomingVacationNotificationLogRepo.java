package ru.abondin.hreasy.platform.repo.notification;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;

@Repository
public interface UpcomingVacationNotificationLogRepo extends ReactiveCrudRepository<UpcomingVacationNotificationLogEntry, Integer> {
    /**
     * @param vacationIds
     * @return
     */
    @Query("select l.vacation from notify.upcoming_vacation_notification_log l where l.vacation in (:vacationIds)")
    Flux<Integer> vacationsIn(List<Integer> vacationIds);
}

