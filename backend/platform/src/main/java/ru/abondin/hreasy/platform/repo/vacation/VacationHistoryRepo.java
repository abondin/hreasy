package ru.abondin.hreasy.platform.repo.vacation;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VacationHistoryRepo extends ReactiveCrudRepository<VacationEntry.VacationHistoryEntry, Integer> {

}
