package ru.abondin.hreasy.platform.repo.dict;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Persistence for active and historical project workstreams.
 */
@Repository
public interface ProjectWorkstreamRepo extends ReactiveCrudRepository<ProjectWorkstreamEntry, Integer> {
    @Query("select * from proj.project_workstream where deleted_at is null order by project_id, display_name")
    Flux<ProjectWorkstreamEntry> findActive();

    @Query("select * from proj.project_workstream where project_id = :projectId and deleted_at is null order by display_name")
    Flux<ProjectWorkstreamEntry> findActiveByProjectId(int projectId);
}
