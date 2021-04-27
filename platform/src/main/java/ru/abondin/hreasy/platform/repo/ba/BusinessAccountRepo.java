package ru.abondin.hreasy.platform.repo.ba;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface BusinessAccountRepo extends ReactiveCrudRepository<BusinessAccountEntry, Integer> {

    @Query("select ba.*, trim(concat_ws(' ', re.lastname, re.firstname, re.patronymic_name)) as responsibleEmployeeName " +
            " from business_account ba left join employee re on ba.responsible_employee=re.id" +
            " order by name")
    Flux<BusinessAccountEntryView> findDetailed();

    @Query("select ba.*, trim(concat_ws(' ', re.lastname, re.firstname, re.patronymic_name)) as responsibleEmployeeName " +
            " from business_account ba left join employee re on ba.responsible_employee=re.id" +
            " where archived_at is null order by name")
    Flux<BusinessAccountEntryView> findActive();


}
