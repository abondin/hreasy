package ru.abondin.hreasy.platform.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusinessErrorDto {
    private String code;
    private String message;
    private Map<String, String> args = new HashMap<>();
}
