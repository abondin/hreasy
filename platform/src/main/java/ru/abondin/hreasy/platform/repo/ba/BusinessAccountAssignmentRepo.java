package ru.abondin.hreasy.platform.repo.ba;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessAccountAssignmentRepo extends ReactiveCrudRepository<BusinessAccountAssignmentEntry, Integer> {
}
