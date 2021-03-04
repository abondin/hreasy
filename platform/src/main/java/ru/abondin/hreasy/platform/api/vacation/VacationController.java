package ru.abondin.hreasy.platform.api.vacation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.vacation.VacationService;
import ru.abondin.hreasy.platform.service.vacation.dto.VacationCreateOrUpdateDto;
import ru.abondin.hreasy.platform.service.vacation.dto.VacationDto;

import javax.validation.Valid;

@RestController()
@RequestMapping("/api/v1/vacations")
@RequiredArgsConstructor
@Slf4j
public class VacationController {
    private final VacationService vacationService;

    @GetMapping("")
    @ResponseBody
    public Flux<VacationDto> findAll() {
        return
                AuthHandler.currentAuth().flatMapMany(auth -> vacationService.findAll(auth, new VacationService.VacationFilter()));
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

}
