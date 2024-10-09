package ru.abondin.hreasy.platform.api.employee;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.EmployeeService;
import ru.abondin.hreasy.platform.service.FileStorage;
import ru.abondin.hreasy.platform.service.admin.employee.AdminEmployeeService;
import ru.abondin.hreasy.platform.service.dto.CurrentProjectRole;
import ru.abondin.hreasy.platform.service.dto.EmployeeDto;
import ru.abondin.hreasy.platform.service.dto.EmployeeUpdateTelegramBody;

import java.util.HashMap;

import static ru.abondin.hreasy.platform.service.admin.dict.AdminDictOfficeLocationService.OFFICE_LOCATION_MAP_RESOURCE_TYPE;
import static ru.abondin.hreasy.platform.service.admin.dict.AdminDictOfficeLocationService.getOfficeLocationMapFileName;


@RestController()
@RequestMapping("/api/v1/employee")
@RequiredArgsConstructor
@Slf4j
public class EmployeeController {

    private final EmployeeService emplService;
    private final AdminEmployeeService adminEmployeeService;

    private final FileStorage fileStorage;

    @Operation(summary = "Get basic information about all employees of the company")
    @GetMapping("")
    @ResponseBody
    public Flux<EmployeeDto> employees(@RequestParam(defaultValue = "false") boolean includeFired) {
        var locationMaps = new HashMap<Integer, Boolean>();
        return AuthHandler.currentAuth().flatMapMany(
                auth -> emplService.findAll(auth, includeFired).map(
                        empl -> {
                            empl.setHasAvatar(fileStorage.fileExists("avatars", empl.getId() + ".png"));
                            if (empl.getOfficeLocation() == null) {
                                empl.setHasOfficeLocationMap(false);
                            } else {
                                empl.setHasOfficeLocationMap(locationMaps.compute(
                                        empl.getOfficeLocation().getId(),
                                        (k, v) -> v == null ?
                                                fileStorage.fileExists(OFFICE_LOCATION_MAP_RESOURCE_TYPE,
                                                        getOfficeLocationMapFileName(empl.getOfficeLocation().getId()))
                                                : v
                                ));
                            }
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

    @Operation(summary = "Get unique set of current project roles for all employees")
    @GetMapping("/current_project_roles")
    @ResponseBody
    public Flux<CurrentProjectRole> currentUserRoles() {
        return AuthHandler.currentAuth().flatMapMany(auth -> emplService.currentUserRoles(auth));
    }

    @Operation(summary = "Update current project for employee")
    @PutMapping("/{employeeId}/currentProject")
    @ResponseBody
    public Mono<Integer> updateCurrentProject(@PathVariable int employeeId,
                                              @RequestBody(required = false) UpdateCurrentProjectBody newCurrentProject) {
        return AuthHandler.currentAuth().flatMap(auth -> adminEmployeeService.updateCurrentProject(employeeId, newCurrentProject, auth));
    }

    /**
     * @param employeeId
     * @param body
     * @return employeeId
     */
    @Operation(summary = "Update telegram account for employee")
    @PutMapping("/{employeeId}/telegram")
    @ResponseBody
    public Mono<Integer> updateTelegram(@PathVariable int employeeId, @NonNull @RequestBody EmployeeUpdateTelegramBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> adminEmployeeService.updateTelegram(auth, employeeId, body));
    }

}
