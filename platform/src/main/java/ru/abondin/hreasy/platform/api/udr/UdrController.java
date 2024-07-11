package ru.abondin.hreasy.platform.api.udr;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.udr.UdrService;
import ru.abondin.hreasy.platform.service.udr.dto.UdrDto;

@RestController()
@RequestMapping("/api/v1/udr")
@RequiredArgsConstructor
@Slf4j
public class UdrController {
    private final UdrService service;

    @GetMapping("/{id}")
    @Operation(summary = "Get User Defined Registry (UDR) by id")
    public Mono<UdrDto> udr(@PathVariable int id) {
        return AuthHandler.currentAuth().flatMap(authContext -> service.getUdr(authContext, id));
    }
}
