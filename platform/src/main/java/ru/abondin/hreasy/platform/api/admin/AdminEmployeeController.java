package ru.abondin.hreasy.platform.api.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.admin.employee.AdminEmployeeService;
import ru.abondin.hreasy.platform.service.admin.employee.dto.CreateOrUpdateEmployeeBody;
import ru.abondin.hreasy.platform.service.admin.employee.dto.EmployeeWithAllDetailsDto;

/**
 * Add new employee record and update existing one
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/admin/employees")
public class AdminEmployeeController {

    private final AdminEmployeeService employeeService;


    @GetMapping
    public Flux<EmployeeWithAllDetailsDto> all() {
        return AuthHandler.currentAuth().flatMapMany(auth -> employeeService.findAll(auth));
    }

    @GetMapping("/{employeeId}")
    public Mono<EmployeeWithAllDetailsDto> get(@PathVariable int employeeId) {
        return AuthHandler.currentAuth().flatMap(auth -> employeeService.get(auth, employeeId));
    }

    @PostMapping
    public Mono<Integer> createBA(@RequestBody CreateOrUpdateEmployeeBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> employeeService.create(auth, body));
    }

    @PutMapping("/{employeeId}")
    public Mono<Integer> updateBA(@PathVariable int employeeId, @RequestBody CreateOrUpdateEmployeeBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> employeeService.update(auth, employeeId, body));
    }

}
