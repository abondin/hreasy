package ru.abondin.hreasy.platform.api.udr;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.udr.JuniorRegistryService;
import ru.abondin.hreasy.platform.service.udr.dto.AddToJuniorRegistryBody;
import ru.abondin.hreasy.platform.service.udr.dto.JuniorDto;

@RestController()
@RequestMapping("/api/v1/udr")
@RequiredArgsConstructor
@Slf4j
public class UdrController {
    private final JuniorRegistryService service;

    @GetMapping("/juniors/")
    @Operation(summary = "Get juniors list")
    public Flux<JuniorDto> juniors() {
        return AuthHandler.currentAuth().flatMapMany(authContext -> service.juniors(authContext));
    }

    @PostMapping("/juniors/{juniorEmployeeId}")
    @Operation(summary = "Add junior to registry")
    public Mono<Integer> addToRegistry(@PathVariable Integer juniorEmployeeId, @RequestBody AddToJuniorRegistryBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> service.addToRegistry(auth, juniorEmployeeId, body));
    }
}
