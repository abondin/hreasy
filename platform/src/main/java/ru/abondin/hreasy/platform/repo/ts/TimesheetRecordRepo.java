package ru.abondin.hreasy.platform.repo.ts;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimesheetRecordRepo extends ReactiveCrudRepository<TimesheetRecordEntry, Integer> {
}
