package ru.abondin.hreasy.platform.repo.assessment;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface AssessmentFormRepo extends ReactiveCrudRepository<AssessmentFormEntry, Integer> {

    Flux<AssessmentFormEntry> findByAssessmentId(int assessmentId);
}
