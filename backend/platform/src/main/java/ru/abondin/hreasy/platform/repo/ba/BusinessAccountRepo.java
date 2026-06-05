package ru.abondin.hreasy.platform.repo.ba;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.repo.manager.ManagerRepo;

@Repository
public interface BusinessAccountRepo extends ReactiveCrudRepository<BusinessAccountEntry, Integer> {

    String FIND_QUERY_PREFIX = """
            select ba.*, mgs.managers_json from ba.business_account ba
            left join (
            """+ ManagerRepo.AGGREGATED_MANAGERS_BY_OBJECT+
            """
                ) mgs on ba.id=mgs.object_id and mgs.object_type='business_account' 
            """;

    @Query(FIND_QUERY_PREFIX +
            "  order by name")
    Flux<BusinessAccountEntryView> findDetailed();

    @Query(FIND_QUERY_PREFIX +
            "  where ba.archived != true order by name")
    Flux<BusinessAccountEntryView> findActive();


    @Query(FIND_QUERY_PREFIX + " where ba.id=:baId")
    Mono<BusinessAccountEntryView> findDetailedById(int baId);
}
