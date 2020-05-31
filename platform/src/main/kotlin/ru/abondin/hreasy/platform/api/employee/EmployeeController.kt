package ru.abondin.hreasy.platform.api.employee

import io.swagger.v3.oas.annotations.Operation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.abondin.hreasy.platform.sec.AuthHandler
import ru.abondin.hreasy.platform.sec.validateUpdateCurrentProject
import ru.abondin.hreasy.platform.service.EmployeeService
import ru.abondin.hreasy.platform.service.FileStorage

@RestController()
@RequestMapping("/api/v1/employee")
class EmployeeController {


    @Autowired
    lateinit var emplService: EmployeeService;

    @Autowired
    lateinit var fileStorage: FileStorage;

    @Operation(summary = "Get basic information about all employees of the company")
    @GetMapping("")
    @ResponseBody
    fun employees(@RequestParam includeFired: Boolean = false): Flux<EmployeeDto> =
            AuthHandler.currentAuth().flatMapMany { auth ->
                emplService.findAll(auth, includeFired).map { empl ->
                    empl.hasAvatar = fileStorage.fileExists("avatars", "${empl.id}.png");
                    return@map empl;
                }
            };

    @Operation(summary = "Get basic information about given employee")
    @GetMapping("/{employeeId}")
    @ResponseBody
    fun employee(@PathVariable employeeId: Int): Mono<EmployeeDto> =
            AuthHandler.currentAuth().flatMap { auth ->
                validateUpdateCurrentProject(auth, employeeId);
                emplService.find(employeeId, auth).map { empl ->
                    empl.hasAvatar = fileStorage.fileExists("avatars", "${empl.id}.png");
                    return@map empl;
                }
            };

    @Operation(summary = "Update current project for employee")
    @PutMapping("/{employeeId}/currentProject/{newCurrentProjectId}")
    @ResponseBody
    fun updateCurrentProject(@PathVariable employeeId: Int,
                             @PathVariable newCurrentProjectId: Int
    ): Mono<Boolean> =
            AuthHandler.currentAuth().flatMap { auth ->
                emplService.updateCurrentProject(employeeId, newCurrentProjectId, auth)
            };

}
