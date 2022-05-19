package ru.abondin.hreasy.platform.repo.employee;

import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;
import ru.abondin.hreasy.platform.service.skills.dto.RatingsMapper;

@Data
@Table("empl.v_employee_detailed")
public class EmployeeDetailedEntry extends EmployeeEntry {
    private String departmentName;
    private String positionName;
    private String positionCategory;
    private String levelName;
    private String currentProjectName;
    private String officeLocationName;
    private Integer baId;
    private String baName;

    /**
     * All employee skills with ratings assembled to string
     *
     * @see RatingsMapper#parseAssembledSkills
     */
    private String aggregatedSkills;
}
