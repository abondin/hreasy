package ru.abondin.hreasy.platform.repo.sec;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface SecPasswdHistoryRepo extends ReactiveCrudRepository<SecPasswdHistoryEntry, Integer> {
}
