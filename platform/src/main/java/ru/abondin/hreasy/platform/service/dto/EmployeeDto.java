package ru.abondin.hreasy.platform.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.abondin.hreasy.platform.service.skills.dto.SkillDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Employee information, available to any authenticated user
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDto {
    private Integer id;
    private String lastname;
    private String firstname;
    private String patronymicName;
    private String displayName;
    private LocalDate birthday;
    private String sex;
    private SimpleDictDto department;
    private CurrentProjectDictDto currentProject;
    private SimpleDictDto position;
    private SimpleDictDto officeLocation;
    private SimpleDictDto ba;
    private String email;
    private String phone;
    private String skype;
    private String telegram;
    private boolean hasAvatar;
    private List<SkillDto> skills = new ArrayList<>();
}
