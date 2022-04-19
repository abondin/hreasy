package ru.abondin.hreasy.platform.repo.techprofile;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@Repository
public interface TechprofileLogRepo extends ReactiveCrudRepository<TechprofileLogEntry, Integer> {
    @Query("update techprofile.techprofile_log set deleted_at=:deletedAt, deleted_by=:deletedBy" +
            " where employee=:employeeId and filename=:filename and deleted_at is null")
    Mono<Integer> markAsDeleted(int employeeId, String filename, OffsetDateTime deletedAt, int deletedBy);
}
