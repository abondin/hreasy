package ru.abondin.hreasy.platform.api.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.salary.SalaryRequestAdminService;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestClosedPeriodDto;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestDto;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/admin/salaries/requests")
public class AdminSalaryRequestController {
    private final SalaryRequestAdminService requestService;

    @GetMapping("/{period}")
    public Flux<SalaryRequestDto> findAll(@PathVariable int period) {
        return AuthHandler.currentAuth().flatMapMany(auth -> requestService.findAll(auth, period));
    }

    @PostMapping("/periods/{period}/close")
    public Mono<Integer> closeSalaryRequestPeriod(@PathVariable int period,
                                             @RequestBody OvertimeAdminController.CloseOvertimePeriodBody body) {
        return AuthHandler.currentAuth().flatMap(auth ->
                requestService.closeSalaryRequestPeriod(auth, period, body.getComment()));
    }

    @PostMapping("/periods/{period}/reopen")
    public Mono<Void> reopenSalaryRequestPeriod(@PathVariable int period,
                                           @RequestBody OvertimeAdminController.ReopenOvertimePeriodBody body) {
        return AuthHandler.currentAuth().flatMap(auth ->
                requestService.reopenSalaryRequestPeriod(auth, period, body.getComment()));
    }

}
