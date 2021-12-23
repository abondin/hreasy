package ru.abondin.hreasy.platform.service.vacation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.vacation.VacationEntry;
import ru.abondin.hreasy.platform.repo.vacation.VacationHistoryRepo;
import ru.abondin.hreasy.platform.repo.vacation.VacationRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.mapper.VacationDtoMapper;
import ru.abondin.hreasy.platform.service.vacation.dto.EmployeeVacationShort;
import ru.abondin.hreasy.platform.service.vacation.dto.MyVacationDto;
import ru.abondin.hreasy.platform.service.vacation.dto.VacationCreateOrUpdateDto;
import ru.abondin.hreasy.platform.service.vacation.dto.VacationDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        private List<Integer> selectedYears = new ArrayList<>();
    }

    private final DateTimeService dateTimeService;
    private final VacationSecurityValidator validator;
    private final VacationRepo vacationRepo;
    private final VacationHistoryRepo historyRepo;
    private final VacationDtoMapper mapper;

    public Flux<VacationDto> findAll(AuthContext auth, VacationFilter filter) {
        return validator.validateCanViewOvertimes(auth).flatMapMany((v) -> vacationRepo.findAll(
                yearsOrDefault(filter.getSelectedYears()), dateTimeService.now()
        ).map(e -> mapper.vacationToDto(e)));
    }

    public Flux<EmployeeVacationShort> currentOrFutureVacations(int employeeId, AuthContext auth) {
        var now = dateTimeService.now();
        var today = now.toLocalDate();
        return vacationRepo.findFuture(employeeId, now)
                .filter(e -> VacationDto.VacationStatus.isActive(e.getStatus()))
                .map(e -> {
                    var dto = mapper.toEmployeeVacationShortDto(e);
                    dto.setCurrent(
                            // Check if start date is today or early
                            (dto.getStartDate() == null || !e.getStartDate().isAfter(today))
                                    && // Check if endDate date is today or later
                                    (dto.getEndDate() == null || !e.getEndDate().isBefore(today))
                    );
                    return dto;
                });
    }

    public Flux<MyVacationDto> my(AuthContext auth) {
        var now = dateTimeService.now();
        return vacationRepo.findFuture(auth.getEmployeeInfo().getEmployeeId(), now).map(e -> mapper.toMyDto(e));
    }

    @Transactional
    public Mono<Integer> create(AuthContext auth, int employeeId, VacationCreateOrUpdateDto body) {
        log.info("Create new overtime item: auth={},empl={},body={}", auth.getUsername(), employeeId, body);
        var now = dateTimeService.now();
        return validator.validateCanEditOvertimes(auth).flatMap((v) -> {
            var entry = mapper.toEntry(body);
            entry.setCreatedAt(now);
            entry.setCreatedBy(auth.getEmployeeInfo().getEmployeeId());
            entry.setUpdatedAt(entry.getCreatedAt());
            entry.setUpdatedBy(entry.getCreatedBy());
            entry.setEmployee(employeeId);
            return vacationRepo.save(entry).flatMap(vacation -> {
                var history = mapper.history(vacation);
                history.setCreatedAt(now);
                history.setCreatedBy(auth.getEmployeeInfo().getEmployeeId());
                return historyRepo.save(history).map(VacationEntry.VacationHistoryEntry::getVacationId);
            });
        });
    }

    @Transactional
    public Mono<Integer> update(AuthContext auth, int employeeId, int vacationId, VacationCreateOrUpdateDto body) {
        log.info("Update overtime item: auth={},empl={},vacationId={}, body={}", auth.getUsername(), employeeId, vacationId, body);
        var now = dateTimeService.now();
        return validator.validateCanEditOvertimes(auth)
                .flatMap(v -> vacationRepo.findById(vacationId))
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(vacationId))))
                .flatMap((entry) -> {
                    mapper.copyToEntry(body, entry);
                    entry.setId(vacationId);
                    entry.setUpdatedAt(now);
                    entry.setUpdatedBy(auth.getEmployeeInfo().getEmployeeId());
                    entry.setEmployee(employeeId);
                    return vacationRepo.save(entry).flatMap(vacation -> {
                        var history = mapper.history(vacation);
                        history.setCreatedAt(now);
                        history.setCreatedBy(auth.getEmployeeInfo().getEmployeeId());
                        history.setVacationId(vacation.getId());
                        return historyRepo.save(history).map(VacationEntry.VacationHistoryEntry::getVacationId);
                    });
                });
    }

    /**
     * @param filteredYears
     * @return filteredYears if not empty or last 3 years include current one
     */
    public List<Integer> yearsOrDefault(List<Integer> filteredYears) {
        if (filteredYears.isEmpty()) {
            var currentYear = dateTimeService.now().getYear();
            return Arrays.asList(currentYear - 1, currentYear, currentYear + 1);
        } else {
            return filteredYears;
        }
    }

}
