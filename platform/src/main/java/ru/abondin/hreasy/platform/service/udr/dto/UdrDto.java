package ru.abondin.hreasy.platform.service.udr.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UdrDto {
    private Integer id;
    private String name;
    private Integer ownerId;
    private String description;
    private List<String> employeeFields = new ArrayList<>();
    private List<UdrCustomFieldConf> customFields = new ArrayList<>();
}
