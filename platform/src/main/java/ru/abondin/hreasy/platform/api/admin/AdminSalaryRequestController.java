package ru.abondin.hreasy.platform.api.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.salary.SalaryRequestService;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestDto;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/admin/salaries/requests")
public class AdminSalaryRequestController {
    private final SalaryRequestService requestService;

    @GetMapping()
    public Flux<SalaryRequestDto> findAll() {
        return AuthHandler.currentAuth().flatMapMany(auth -> requestService.findAll(auth));
    }
}
