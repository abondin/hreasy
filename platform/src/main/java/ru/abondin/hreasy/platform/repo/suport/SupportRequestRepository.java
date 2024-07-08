package ru.abondin.hreasy.platform.repo.suport;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupportRequestRepository extends ReactiveCrudRepository<SupportRequestEntry, Integer> {
}

