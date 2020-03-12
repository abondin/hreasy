package ru.abondin.hreasy.platform.api.employee

import io.swagger.v3.oas.annotations.Operation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.abondin.hreasy.platform.sec.AuthHandler
import ru.abondin.hreasy.platform.service.EmployeeService
import ru.abondin.hreasy.platform.service.FileStorage

@RestController()
@RequestMapping("/api/v1/employee")
class EmployeeController {


    @Autowired
    lateinit var emplService: EmployeeService;

    @Operation(summary = "Get basic informEation about all employees of the company")
    @GetMapping("")
    @ResponseBody
    fun employee(@RequestParam includeFired: Boolean = false): Flux<EmployeeDto> =
            AuthHandler.currentAuth().flatMapMany { auth -> emplService.findAll(auth, includeFired) };

}