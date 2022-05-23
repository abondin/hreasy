package ru.abondin.hreasy.platform.api.ba;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.service.ba.BusinessAccountService;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

/**
 * Helps project managers to share human resources between different projects.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/business_account")
public class BusinessAccountController {

    private final BusinessAccountService baService;

    @GetMapping
    public Flux<SimpleDictDto> allActive() {
        return baService.findAllAsSimpleDict(false);
    }

}
