package ru.abondin.hreasy.platform.api.salary;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.salary.SalaryRequestService;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestClosedPeriodDto;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestDto;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestReportBody;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/salaries/requests")
public class SalaryRequestsController {
    private final SalaryRequestService requestService;

    @PostMapping()
    public Mono<Integer> report(@RequestBody SalaryRequestReportBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> requestService.report(auth, body));
    }

    @DeleteMapping("/{period}/{requestId}")
    public Mono<Integer> delete(@PathVariable int period, @PathVariable int requestId) {
        return AuthHandler.currentAuth().flatMap(auth -> requestService.delete(auth, period, requestId));
    }

    @GetMapping("/{period}")
    public Flux<SalaryRequestDto> my(@PathVariable int period) {
        return AuthHandler.currentAuth().flatMapMany(auth -> requestService.findMy(auth, period));
    }

    @GetMapping("/{period}/{requestId}")
    public Mono<SalaryRequestDto> get(@PathVariable int period, @PathVariable int requestId) {
        return AuthHandler.currentAuth().flatMap(auth -> requestService.get(auth, requestId));
    }


    @GetMapping("/periods")
    public Flux<SalaryRequestClosedPeriodDto> getClosedPeriods() {
        return AuthHandler.currentAuth().flatMapMany(auth ->
                requestService.getClosedSalaryRequestPeriods(auth));
    }
}
