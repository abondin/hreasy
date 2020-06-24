package ru.abondin.hreasy.platform.api.employee;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.EmployeeService;
import ru.abondin.hreasy.platform.service.FileStorage;
import ru.abondin.hreasy.platform.service.dto.EmployeeDto;

import static ru.abondin.hreasy.platform.sec.SecurityUtils.validateUpdateCurrentProject;

@RestController()
@RequestMapping("/api/v1/employee")
@RequiredArgsConstructor
@Slf4j
public class EmployeeController {

    private final EmployeeService emplService;

    private final FileStorage fileStorage;

    @Operation(summary = "Get basic information about all employees of the company")
    @GetMapping("")
    @ResponseBody
    public Flux<EmployeeDto> employees(@RequestParam(defaultValue = "false") boolean includeFired) {
        return AuthHandler.currentAuth().flatMapMany(
                auth -> emplService.findAll(auth, includeFired).map(
                        empl -> {
                            empl.setHasAvatar(fileStorage.fileExists("avatars", empl.getId() + ".png"));
                            return empl;
                        }
                )
        );
    }

    @Operation(summary = "Get basic information about given employee")
    @GetMapping("/{employeeId}")
    @ResponseBody
    public Mono<EmployeeDto> employee(@PathVariable int employeeId) {
        return AuthHandler.currentAuth().flatMap(auth -> emplService.find(employeeId, auth)
                .map(empl -> {
                            empl.setHasAvatar(fileStorage.fileExists("avatars", empl.getId() + ".png"));
                            return empl;
                        }
                )
        );
    }

    @Operation(summary = "Update current project for employee")
    @PutMapping("/{employeeId}/currentProject/{newCurrentProjectId}")
    @ResponseBody
    public Mono<Boolean> updateCurrentProject(@PathVariable int employeeId,
                                              @PathVariable int newCurrentProjectId) {
        return AuthHandler.currentAuth().flatMap(auth -> {
            validateUpdateCurrentProject(auth, employeeId);
            return emplService.updateCurrentProject(employeeId, newCurrentProjectId, auth);
        });
    }

    @Operation(summary = "Reset current project for employee")
    @PutMapping("/{employeeId}/currentProject/reset")
    @ResponseBody
    public Mono<Boolean> resetCurrentProject(@PathVariable int employeeId) {
        return AuthHandler.currentAuth().flatMap(auth -> {
            validateUpdateCurrentProject(auth, employeeId);
            return emplService.updateCurrentProject(employeeId, null, auth);
        });
    }
}
