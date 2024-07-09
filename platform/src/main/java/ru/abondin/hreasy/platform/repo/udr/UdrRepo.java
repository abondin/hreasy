package ru.abondin.hreasy.platform.repo.udr;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface UdrRepo extends ReactiveCrudRepository<UdrEntry, Integer> {
}
