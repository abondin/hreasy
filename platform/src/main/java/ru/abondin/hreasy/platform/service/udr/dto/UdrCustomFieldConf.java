package ru.abondin.hreasy.platform.service.udr.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UdrCustomFieldConf {
    private String key;
    private String displayName;
    private String type;
    private List<String> dicts = new ArrayList<>();
}
