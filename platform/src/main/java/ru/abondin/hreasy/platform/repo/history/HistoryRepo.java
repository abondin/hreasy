package ru.abondin.hreasy.platform.repo.history;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryRepo extends ReactiveCrudRepository<HistoryEntry, Integer> {

}

