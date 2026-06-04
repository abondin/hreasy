package ru.abondin.hreasy.platform.repo.dict;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DictLevelLogRepo extends ReactiveSortingRepository<DictLevelLogEntry, Integer>, ReactiveCrudRepository<DictLevelLogEntry, Integer>  {

}
