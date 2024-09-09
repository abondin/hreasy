package ru.abondin.hreasy.platform.repo.vacation;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.Optional;

@Repository
public interface VacPlanningPeriodRepo extends ReactiveCrudRepository<VacPlanningPeriodEntry, Integer> {

    @Query("SELECT * FROM vac.vac_planning_period WHERE opened_at IS NOT NULL AND closed_at IS NULL")
    Flux<VacPlanningPeriodEntry> findOpened();
}
