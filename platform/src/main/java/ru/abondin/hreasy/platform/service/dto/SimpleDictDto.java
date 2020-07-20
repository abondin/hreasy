package ru.abondin.hreasy.platform.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SimpleDictDto {
    private int id;

    public SimpleDictDto(int id, String name) {
        this.id = id;
        this.name = name;
    }

    private String name;
    /**
     * false if element should be hidden or UI by default
     */
    private boolean active = true;
}
