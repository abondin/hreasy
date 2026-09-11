package ru.abondin.hreasy.platform.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Schema(description = "Employee current project reference.")
public class CurrentProjectDictDto extends SimpleDictDto {
    /**
     * Employee role on project (Backend Developer, Designer, PM, etc)
     */
    @Nullable
    @Schema(description = "Employee role on the project, subject to role-visibility permissions.", example = "Backend Developer", nullable = true)
    private String role;

    public CurrentProjectDictDto(int id, String name, @Nullable String role) {
        super(id, name);
        this.role = role;
    }
}
