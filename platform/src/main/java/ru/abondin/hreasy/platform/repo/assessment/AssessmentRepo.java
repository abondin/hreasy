package ru.abondin.hreasy.platform.repo.assessment;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import ru.abondin.hreasy.platform.repo.ba.BusinessAccountAssignmentEntry;

@Repository
public interface AssessmentRepo extends ReactiveCrudRepository<AssessmentEntry, Integer> {
}
