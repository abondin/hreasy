package ru.abondin.hreasy.platform.api.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.salary.AdminSalaryRequestExportService;
import ru.abondin.hreasy.platform.service.salary.AdminSalaryRequestService;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestDto;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestImplementBody;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestRejectBody;

import java.util.Locale;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/admin/salaries/requests")
public class AdminSalaryRequestController {

    private final AdminSalaryRequestService requestAdminService;
    private final AdminSalaryRequestExportService exportService;

    @GetMapping("/{period}")
    public Flux<SalaryRequestDto> findAll(@PathVariable int period) {
        return AuthHandler.currentAuth().flatMapMany(auth -> requestAdminService.findAll(auth, period));
    }

    @GetMapping("/{period}/export")
    public Mono<Resource> export(@PathVariable int period, Locale locale) {
        return AuthHandler.currentAuth().flatMap(auth -> exportService.export(period, auth, locale));
    }

    @DeleteMapping("/{salaryRequestId}/implementation")
    public Mono<Integer> resetImplementation(@PathVariable int salaryRequestId) {
        return AuthHandler.currentAuth().flatMap(auth ->
                requestAdminService.resetImplementation(auth, salaryRequestId));
    }

    @PutMapping("/{salaryRequestId}/implement")
    public Mono<Integer> markAsImplemented(@PathVariable int salaryRequestId, @RequestBody SalaryRequestImplementBody body) {
        return AuthHandler.currentAuth().flatMap(auth ->
                requestAdminService.markAsImplemented(auth, salaryRequestId, body));
    }

    @PutMapping("/{salaryRequestId}/reject")
    public Mono<Integer> reject(@PathVariable int salaryRequestId, @RequestBody SalaryRequestRejectBody body) {
        return AuthHandler.currentAuth().flatMap(auth ->
                requestAdminService.reject(auth, salaryRequestId, body));
    }

    @PostMapping("/periods/{period}/close")
    public Mono<Integer> closeSalaryRequestPeriod(@PathVariable int period,
                                                  @RequestBody OvertimeAdminController.CloseOvertimePeriodBody body) {
        return AuthHandler.currentAuth().flatMap(auth ->
                requestAdminService.closeSalaryRequestPeriod(auth, period, body.getComment()));
    }

    @PostMapping("/periods/{period}/reopen")
    public Mono<Void> reopenSalaryRequestPeriod(@PathVariable int period,
                                                @RequestBody OvertimeAdminController.ReopenOvertimePeriodBody body) {
        return AuthHandler.currentAuth().flatMap(auth ->
                requestAdminService.reopenSalaryRequestPeriod(auth, period, body.getComment()));
    }

}
