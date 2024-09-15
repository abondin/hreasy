package ru.abondin.hreasy.platform.repo.dict.office_map;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.repo.dict.DictOfficeLocationEntry;

@Repository
public interface OfficeLocationMapRepo extends ReactiveCrudRepository<OfficeLocationMapEntry, Integer> {
}
