package ru.abondin.hreasy.platform.repo.employee;

import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("v_employee_detailed")
public class EmployeeDetailedEntry extends EmployeeEntry {
    private String departmentName;
    private String positionName;
    private String positionCategory;
    private String levelName;
    private String currentProjectName;
    private String officeLocationName;
    /**
     * Joined to one String information about all skills with average rating for each skill.
     * <p>The format is: SKILL_1_ID|$|SKILL_1_NAME|$|G_1_ID|$|G_1_NAME|$|AVG|$|CNT|#|SKILL_2_ID|$|SKILL_2_NAME|$|G_2_ID|$|G_2_NAME|$|AVG|$|CNT|#|...</p>
     * <ul>
     *     <li>SKILL_1_ID, SKILL_2_ID - employees skill ids</li>
     *     <li>SKILL_1_NAME, SKILL_2_NAME - employees skill names</li>
     *     <li>G_1_ID, G_2_ID - group id if skill</li>
     *     <li>G_1_NAME, G_2_NAME - group name if skill</li>
     *     <li>AVG - skill average rating</li>
     *     <li>CNT - ratings count (everyone can rating your skill)</li>
     *     <li>|$| - delimiter between elements of one skill</li>
     *     <li>|#| - delimiter between skills</li>
     * </ul>
     * <p>Example: Java|$|1|$|5|#|Java Script|$|0|$|0|#|Python|$|2|$|4.5</p>
     */
    private String aggregatedSkills;
}
