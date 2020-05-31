package ru.abondin.hreasy.platform.sec

import org.springframework.security.access.AccessDeniedException
import ru.abondin.hreasy.platform.config.AuthContext


fun validateUploadAvatar(auth: AuthContext, employeeId: Int) {
    if (!auth.authorities.contains("update_avatar") && employeeId != auth.employeeInfo?.id) {
        throw AccessDeniedException("Only avatar owner or user with permission update_avatar can update the avatar");
    }
}

fun validateUpdateCurrentProject(auth: AuthContext, employeeId: Int) {
    if (!auth.authorities.contains("update_current_project_global") && employeeId != auth.employeeInfo?.id) {
        throw AccessDeniedException("Only logged in user or user with permission update_current_project_global can update the current project");
    }
}