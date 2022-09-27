package ru.abondin.hreasy.platform.repo.ba;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface BusinessAccountAssignmentRepo extends ReactiveCrudRepository<BusinessAccountAssignmentEntry, Integer> {
    @Query("select * from ba.v_ba_assignment where period=:period and business_account=businessAccount")
    Flux<BusinessAccountAssignmentView> findInBusinessAccount(@Param("period") int period, @Param("businessAccount") int businessAccount);
}
