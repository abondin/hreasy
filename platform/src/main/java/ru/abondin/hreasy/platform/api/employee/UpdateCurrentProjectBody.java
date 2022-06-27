package ru.abondin.hreasy.platform.api.employee;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.abondin.hreasy.platform.service.dto.CurrentProjectDictDto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

/**
 * Update employee's current project body
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCurrentProjectBody {
    @NotNull
    private Integer id;

    /**
     * @see CurrentProjectDictDto#getRole()
     */
    @Null
    private String role;
}
