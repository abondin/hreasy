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

    public static Mono<Boolean> validateUpdateCurrentProject(AuthContext auth, int employeeId) {
        return Mono.defer(() -> {
            if (!auth.getAuthorities().contains("update_current_project_global") && employeeId != auth.getEmployeeInfo().getEmployeeId()) {
                return Mono.error(new AccessDeniedException("Only logged in user or user with permission update_current_project_global can update the current project"));
            }
            return Mono.just(true);
        });
    }

    public static Mono validateEditOvertimeItem(AuthContext auth, int employeeId) {
        return Mono.defer(() -> {
            if (!auth.getAuthorities().contains("overtime_edit") && employeeId != auth.getEmployeeInfo().getEmployeeId()) {
                return Mono.error(new AccessDeniedException("Only logged in user or user with permission overtime_edit can update the overtime item"));
            }
            return Mono.just(true);
        });
    }

    public static Mono validateViewOvertime(AuthContext auth, int employeeId) {
        return Mono.defer(() -> {
            if (!auth.getAuthorities().contains("overtime_view") && employeeId != auth.getEmployeeInfo().getEmployeeId()) {
                return Mono.error(new AccessDeniedException("Only logged in user or user with permission overtime_view can view the overtime repotr"));
            }
            return Mono.just(true);
        });
    }


    public static Mono validateViewOvertimeSummary(AuthContext auth) {
        return Mono.defer(() -> {
            if (!auth.getAuthorities().contains("overtime_view")) {
                return Mono.error(new AccessDeniedException("Only user with permission overtime_view can view the overtime report"));
            }
            return Mono.just(true);
        });
    }

}
