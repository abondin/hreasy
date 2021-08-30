package ru.abondin.hreasy.platform.repo.assessment;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssessmentFormHistoryRepo extends ReactiveCrudRepository<AssessmentFormHistoryEntry, Integer> {
}
