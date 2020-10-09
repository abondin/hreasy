package ru.abondin.hreasy.platform.sec;

import org.springframework.security.access.AccessDeniedException;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;


public class SecurityUtils {


    public static Mono<Boolean> validateUploadAvatar(AuthContext auth, int employeeId) {
        return Mono.defer(() -> {
            if (!auth.getAuthorities().contains("update_avatar") && employeeId != auth.getEmployeeInfo().getEmployeeId()) {
                return Mono.error(new AccessDeniedException("Only avatar owner or user with permission update_avatar can update the avatar"));
            }
            return Mono.just(true);
        });
    }

}
