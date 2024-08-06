package ru.abondin.hreasy.platform.api.udr;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.udr.JuniorRegistryService;
import ru.abondin.hreasy.platform.service.udr.dto.*;

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

    @DeleteMapping("/juniors/{registryId}/graduate")
    @Operation(summary = "Cancel junior graduation")
    public Mono<Integer> cancelGraduation(@PathVariable int registryId) {
        return AuthHandler.currentAuth().flatMap(auth -> service.cancelGraduation(auth, registryId));
    }

    @PostMapping("/juniors/{registryId}/reports")
    @Operation(summary = "Add junior report")
    public Mono<Integer> addJuniorReport(@PathVariable int registryId, @RequestBody AddJuniorReportBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> service.addJuniorReport(auth, registryId, body));
    }

    @PutMapping("/juniors/{registryId}/reports/{reportId}")
    @Operation(summary = "Update junior report")
    public Mono<Integer> updateJuniorReport(@PathVariable int registryId, @PathVariable int reportId, @RequestBody UpdateJuniorReportBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> service.updateJuniorReport(auth, registryId, reportId, body));
    }

    @DeleteMapping("/juniors/{registryId}/reports/{reportId}")
    @Operation(summary = "Delete junior graduation")
    public Mono<Integer> deleteJuniorReport(@PathVariable int registryId, @PathVariable int reportId) {
        return AuthHandler.currentAuth().flatMap(auth -> service.deleteJuniorReport(auth, registryId, reportId));
    }

}
