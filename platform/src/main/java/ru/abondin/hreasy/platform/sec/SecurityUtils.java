package ru.abondin.hreasy.platform.sec;

import org.springframework.security.access.AccessDeniedException;
import ru.abondin.hreasy.platform.auth.AuthContext;


public class SecurityUtils {


    public static void validateUploadAvatar(AuthContext auth, int employeeId) {
        if (!auth.getAuthorities().contains("update_avatar") && employeeId != auth.getEmployeeInfo().getEmployeeId()) {
            throw new AccessDeniedException("Only avatar owner or user with permission update_avatar can update the avatar");
        }
    }

    public static void validateUpdateCurrentProject(AuthContext auth, int employeeId) {
        if (!auth.getAuthorities().contains("update_current_project_global") && employeeId != auth.getEmployeeInfo().getEmployeeId()) {
            throw new AccessDeniedException("Only logged in user or user with permission update_current_project_global can update the current project");
        }
    }
}
