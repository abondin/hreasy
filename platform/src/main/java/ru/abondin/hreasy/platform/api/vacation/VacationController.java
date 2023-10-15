package ru.abondin.hreasy.platform.api.vacation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.vacation.VacationExportService;
import ru.abondin.hreasy.platform.service.vacation.VacationService;
import ru.abondin.hreasy.platform.service.vacation.dto.EmployeeVacationShort;
import ru.abondin.hreasy.platform.service.vacation.dto.MyVacationDto;
import ru.abondin.hreasy.platform.service.vacation.dto.VacationCreateOrUpdateDto;
import ru.abondin.hreasy.platform.service.vacation.dto.VacationDto;

import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@RestController()
@RequestMapping("/api/v1/vacations")
@RequiredArgsConstructor
@Slf4j
public class VacationController {
    private final VacationService vacationService;
    private final VacationExportService vacationExportService;
    private final DateTimeService dateTimeService;

    @GetMapping("")
    @ResponseBody
    public Flux<VacationDto> findAll(@RequestParam(name = "years[]") List<Integer> years) {
        return
                AuthHandler.currentAuth().flatMapMany(auth -> vacationService.findAll(auth, new VacationService.VacationFilter(years)));
    }

    /**
     * Future or current vacations for current user
     *
     * @return
     */
    @GetMapping("/my")
    @ResponseBody
    public Flux<MyVacationDto> my() {
        return
                AuthHandler.currentAuth().flatMapMany(auth -> vacationService.my(auth));
    }

    /**
     * Future or current vacations for any user
     *
     * @return
     */
    @GetMapping("/{employeeId}/currentOrFuture")
    @ResponseBody
    public Flux<EmployeeVacationShort> currentOrFutureVacations(@PathVariable int employeeId) {
        return
                AuthHandler.currentAuth().flatMapMany(auth -> vacationService.currentOrFutureVacations(employeeId, auth));
    }


    /**
     * @param employeeId
     * @param body
     * @return id of created vacation id
     */
    @PostMapping("/{employeeId}")
    @ResponseBody
    public Mono<Integer> create(@PathVariable int employeeId, @Valid @RequestBody VacationCreateOrUpdateDto body) {
        return AuthHandler.currentAuth().flatMap(auth -> vacationService.create(auth, employeeId, body));
    }

    /**
     * @param employeeId
     * @param vacationId
     * @param body
     * @return id of updated vacation id
     */
    @PutMapping("/{employeeId}/{vacationId}")
    @ResponseBody
    public Mono<Integer> update(@PathVariable int employeeId, @PathVariable int vacationId, @Valid @RequestBody VacationCreateOrUpdateDto body) {
        return AuthHandler.currentAuth().flatMap(auth -> vacationService.update(auth, employeeId, vacationId, body));
    }

    /**
     * Export all
     *
     * @return
     */
    @GetMapping("/export")
    public Mono<Resource> export(@RequestParam(required = false) List<Integer> years, Locale locale) {
        return AuthHandler.currentAuth().flatMap(auth -> vacationExportService
                .export(auth,
                        new VacationExportService.VacationExportFilter(years == null ? Arrays.asList() : years)
                        , locale));
    }

}
