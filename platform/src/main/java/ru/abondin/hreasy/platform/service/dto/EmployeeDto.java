package ru.abondin.hreasy.platform.service.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * Employee information, available to any authenticated user
 */
@Data
public class EmployeeDto {
    private Integer id;
    private String lastname;
    private String firstname;
    private String patronymicName;
    private String displayName;
    private LocalDate birthday;
    private String sex;
    private SimpleDictDto department;
    private SimpleDictDto currentProject;
    private SimpleDictDto position;
    private String email;
    private String phone;
    private String skype;
    private boolean hasAvatar;
}
