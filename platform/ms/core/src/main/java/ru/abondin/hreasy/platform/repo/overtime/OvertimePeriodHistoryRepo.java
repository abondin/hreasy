package ru.abondin.hreasy.platform.repo.overtime;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface OvertimePeriodHistoryRepo extends R2dbcRepository<OvertimePeriodHistoryEntry, Integer> {
}
