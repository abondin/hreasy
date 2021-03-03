package ru.abondin.hreasy.platform.api.vacation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.service.VacationService;
import ru.abondin.hreasy.platform.service.dto.VacationDto;

@RestController()
@RequestMapping("/api/v1/vacations")
@RequiredArgsConstructor
@Slf4j
public class VacationController {
    private final VacationService vacationService;

    @GetMapping("")
    @ResponseBody
    public Flux<VacationDto> findAll() {
        return vacationService.findAll(new VacationService.VacationFilter());
    }
}
