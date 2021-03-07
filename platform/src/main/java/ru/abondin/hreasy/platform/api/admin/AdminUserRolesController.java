package ru.abondin.hreasy.platform.api.admin;


import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.admin.AdminUserRolesService;
import ru.abondin.hreasy.platform.service.admin.dto.UserSecurityInfoDto;

import java.util.List;

@RestController()
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Slf4j
public class AdminUserRolesController {

    private final AdminUserRolesService service;

    @GetMapping()
    public Flux<UserSecurityInfoDto> users(@RequestParam(defaultValue = "false") Boolean includeFired) {
        return AuthHandler.currentAuth().flatMapMany(auth -> service.users(auth, AdminUserListFilter
                .builder().includeFired(includeFired)
                .build()));
    }

    @PutMapping("/roles/{employeeId}")
    public Mono<Integer> updateUserRoles(@PathVariable("employeeId") int employeeId, @RequestBody UserRolesUpdateBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> service.updateRoles(auth, employeeId, body));
    }

    @Data
    @ToString
    public static class UserRolesUpdateBody {
        private List<String> roles;
        private List<Integer> accessibleDepartments;
        private List<Integer> accessibleProjects;
    }

    @Data
    @Builder
    public static class AdminUserListFilter {
        private boolean includeFired = false;
    }

}
