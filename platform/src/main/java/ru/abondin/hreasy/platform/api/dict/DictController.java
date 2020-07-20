package ru.abondin.hreasy.platform.api.dict;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
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
    public Flux<SimpleDictDto> projects() {
        return AuthHandler.currentAuth().flatMapMany(
                auth -> dictService.findProjects(auth));
    }
}
