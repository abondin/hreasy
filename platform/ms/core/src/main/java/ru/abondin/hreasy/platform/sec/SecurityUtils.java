package ru.abondin.hreasy.platform.sec;

import org.springframework.security.access.AccessDeniedException;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;


public class SecurityUtils {


    public static Mono<Boolean> validateUploadAvatar(AuthContext auth, int employeeId) {
        return Mono.defer(() -> {
            // 2021/11/09 By request from HR department deprecate update own avatar...
            //  && employeeId != auth.getEmployeeInfo().getEmployeeId()
            if (!auth.getAuthorities().contains("update_avatar")) {
                return Mono.error(new AccessDeniedException("Only user with permission update_avatar can update the avatar"));
            }
            return Mono.just(true);
        });
    }

}
