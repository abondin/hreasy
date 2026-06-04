package ru.abondin.hreasy.platform.service.support.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SupportGroupConfiguration {
    private List<String> emails = new ArrayList<>();
    private List<String> categories = new ArrayList<>();
}
