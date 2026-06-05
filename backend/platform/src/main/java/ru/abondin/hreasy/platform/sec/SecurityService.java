package ru.abondin.hreasy.platform.sec;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.repo.sec.PermissionRepo;

@Component
@Slf4j
@RequiredArgsConstructor
public class SecurityService {
    private final PermissionRepo permissionRepo;

    @Transactional
    public Flux<String> getPermissions(String email) {
        return permissionRepo.findByEmployeeEmail(email)
                .map(perm -> perm.getPermission());
    }


}
