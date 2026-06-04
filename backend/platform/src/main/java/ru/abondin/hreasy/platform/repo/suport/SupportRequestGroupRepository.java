package ru.abondin.hreasy.platform.repo.suport;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface SupportRequestGroupRepository extends ReactiveCrudRepository<SupportRequestGroupEntry, String> {

    @Query("select * from support.support_request_group where deleted_at is null")
    Flux<SupportRequestGroupEntry> findNotDeleted();

}

