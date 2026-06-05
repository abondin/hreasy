package ru.abondin.hreasy.platform.repo.dict;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DictOrganizationLogRepo extends
        ReactiveSortingRepository<DictOrganizationLogEntry, Integer>, ReactiveCrudRepository<DictOrganizationLogEntry, Integer> {

}
