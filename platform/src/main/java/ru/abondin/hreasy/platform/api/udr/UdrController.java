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
import ru.abondin.hreasy.platform.service.udr.dto.GraduateJuniorBody;
import ru.abondin.hreasy.platform.service.udr.dto.JuniorDto;
import ru.abondin.hreasy.platform.service.udr.dto.UpdateJuniorRegistryBody;

@RestController()
@RequestMapping("/api/v1/udr")
@RequiredArgsConstructor
@Slf4j
public class UdrController {
    private final JuniorRegistryService service;

    @GetMapping("/juniors")
    @Operation(summary = "Get juniors list")
    public Flux<JuniorDto> juniors() {
        return AuthHandler.currentAuth().flatMapMany(service::juniors);
    }

    @GetMapping("/juniors/{registryId}")
    @Operation(summary = "Get juniors list")
    public Mono<JuniorDto> juniorDetailed(@PathVariable int registryId) {
        return AuthHandler.currentAuth().flatMap(auth -> service.juniorDetailed(auth, registryId));
    }

    @PostMapping("/juniors")
    @Operation(summary = "Add junior to registry")
    public Mono<Integer> addToRegistry(@RequestBody AddToJuniorRegistryBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> service.addToRegistry(auth, body));
    }

    @PutMapping("/juniors/{registryId}")
    @Operation(summary = "Update junior registry record")
    public Mono<Integer> updateJunior(@PathVariable int registryId, @RequestBody UpdateJuniorRegistryBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> service.update(auth, registryId, body));
    }
    @PutMapping("/juniors/{registryId}/graduate")
    @Operation(summary = "Graduate junior")
    public Mono<Integer> graduateJunior(@PathVariable int registryId, @RequestBody GraduateJuniorBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> service.graduate(auth, registryId, body));
    }

    @DeleteMapping("/juniors/{registryId}")
    @Operation(summary = "Delete junior from registry")
    public Mono<Integer> deleteJuniorFromRegistry(@PathVariable int registryId) {
        return AuthHandler.currentAuth().flatMap(auth -> service.delete(auth, registryId));
    }

}
