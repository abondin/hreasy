package ru.abondin.hreasy.platform.service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Data
@NoArgsConstructor
public class CurrentProjectDictDto extends SimpleDictDto {
    /**
     * Employee role on project (Backend Developer, Designer, PM, etc)
     */
    @Nullable
    private String role;

    public CurrentProjectDictDto(int id, String name, @Nullable String role) {
        super(id, name);
        this.role = role;
    }
}
