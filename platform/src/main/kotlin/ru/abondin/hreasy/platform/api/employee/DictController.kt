package ru.abondin.hreasy.platform.api.employee

import io.swagger.v3.oas.annotations.Operation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import ru.abondin.hreasy.platform.sec.AuthHandler
import ru.abondin.hreasy.platform.service.DictService

@RestController()
@RequestMapping("/api/v1/dict")
class DictController {

    @Autowired
    lateinit var dictService: DictService;

    @Operation(summary = "All projects")
    @GetMapping("/projects")
    @ResponseBody
    fun projects(@RequestParam includeClosed: Boolean = false): Flux<SimpleDictDto> =
            AuthHandler.currentAuth().flatMapMany { auth ->
                dictService.findProjects(auth, includeClosed);
            };


}

data class SimpleDictDto(
        val id: Int,
        val name: String?
)
