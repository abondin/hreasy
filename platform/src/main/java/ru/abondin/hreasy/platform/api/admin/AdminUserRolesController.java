package ru.abondin.hreasy.platform.api.admin;


import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.admin.AdminUserRolesService;
import ru.abondin.hreasy.platform.service.admin.dto.EmployeeWithSecurityInfoDto;

@RestController()
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Slf4j
public class AdminUserRolesController {

    private final AdminUserRolesService service;

    @GetMapping("")
    public Flux<EmployeeWithSecurityInfoDto> users(@RequestBody(required = false) @Nullable AdminUserListFilter filter) {
        return AuthHandler.currentAuth().flatMapMany(auth -> service.users(auth, filter));
    }


    @Data
    public static class AdminUserListFilter {
        private boolean includeFired = false;
    }

}
