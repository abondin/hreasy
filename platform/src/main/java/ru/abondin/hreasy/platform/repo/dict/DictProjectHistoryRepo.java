package ru.abondin.hreasy.platform.repo.dict;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DictProjectHistoryRepo extends ReactiveCrudRepository<DictProjectEntry.ProjectHistoryEntry, Integer> {

}
