package ru.abondin.hreasy.platform.service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SimpleDictDto{
    private int id;
    private String name;
    /**
     * false if element should be hidden or UI by default
     */
    private boolean active = true;

    public SimpleDictDto(int id, String name) {
        this(id, name, true);
    }

    public SimpleDictDto(int id, String name, boolean active) {
        this.id = id;
        this.name = name;
        this.active = active;
    }

}
