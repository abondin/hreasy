package ru.abondin.hreasy.platform.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.repo.vacation.VacationRepo;
import ru.abondin.hreasy.platform.service.dto.VacationDto;
import ru.abondin.hreasy.platform.service.mapper.VacationDtoMapper;

import java.time.LocalDate;

/**
 * Service to deal with employees vacations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VacationService {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VacationFilter {
        private LocalDate endDateSince = LocalDate.of(2000, 1, 1);
    }

    private final DateTimeService dateTimeService;
    private final VacationRepo vacationRepo;
    private final VacationDtoMapper mapper;

    public Flux<VacationDto> findAll(VacationFilter filter) {
        return vacationRepo.findAll(
                filter.endDateSince
        ).map(e -> mapper.vacationToDto(e));
    }
}
