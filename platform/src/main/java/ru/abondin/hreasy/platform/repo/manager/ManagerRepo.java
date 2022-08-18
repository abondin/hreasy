package ru.abondin.hreasy.platform.repo.manager;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ManagerRepo extends ReactiveCrudRepository<ManagerEntry, Integer> {

}

