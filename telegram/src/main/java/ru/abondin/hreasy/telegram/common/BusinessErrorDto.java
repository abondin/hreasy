package ru.abondin.hreasy.telegram.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BusinessErrorDto {
    private String code;
    private String message;
    private Map<String, String> args = new HashMap<>();
}
