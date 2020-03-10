package ru.abondin.hreasy.platform.api.vacation

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import ru.abondin.hreasy.platform.service.VacationFilter
import ru.abondin.hreasy.platform.service.VacationService

@RestController()
@RequestMapping("/api/v1/vacation")
class VacationController {

    @Autowired
    lateinit var vacationService: VacationService;

    @GetMapping("")
    @ResponseBody
    fun findAll(): Flux<VacationDto> {
        return vacationService.findAll(VacationFilter());
    }

}