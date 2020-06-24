package ru.abondin.hreasy.platform.api.dict;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.DictService;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

@RestController()
@RequestMapping("/api/v1/dict")
@RequiredArgsConstructor
@Slf4j
public class DictController {

    private final DictService dictService;

    @Operation(summary = "All projects")
    @GetMapping("/projects")
    @ResponseBody
    Flux<SimpleDictDto> projects(@RequestParam boolean includeClosed) {
        return AuthHandler.currentAuth().flatMapMany(
                auth -> dictService.findProjects(auth, includeClosed));
    }
}
