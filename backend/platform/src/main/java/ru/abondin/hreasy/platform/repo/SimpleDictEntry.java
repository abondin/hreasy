package ru.abondin.hreasy.platform.repo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

/**
 * Basic representation of any dictionary entry
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimpleDictEntry {
    @Id
    private int id;
    private String name;
}
