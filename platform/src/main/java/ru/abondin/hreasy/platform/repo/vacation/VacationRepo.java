package ru.abondin.hreasy.platform.repo.vacation;

import reactor.core.publisher.Flux;

import java.time.LocalDate;

public interface VacationRepo {
    Flux<VacationView> findAll(LocalDate endDateSince);
}
