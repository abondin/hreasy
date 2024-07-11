package ru.abondin.hreasy.platform.repo.udr;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UdrAccessRepo extends ReactiveCrudRepository<UdrAccessEntry, Integer> {

    @Query("""
            select exists(
                select id from udr.registry r
                    left join udr.registry_access a on r.id = a.registry_id
                    where r.id = :udrId and (
                        r.owner_id = :employeeId or
                        (a.employee_id = :employeeId and a.read_permission = true)
                    ) 
            )
            """)
    Mono<Boolean> hasAccessToUdr(int employeeId, int udrId);

}

