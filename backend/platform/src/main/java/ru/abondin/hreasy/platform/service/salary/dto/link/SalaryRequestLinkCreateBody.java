package ru.abondin.hreasy.platform.service.salary.dto.link;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

/**
 * Data to create a link between two salary requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryRequestLinkCreateBody {
    @NonNull
    private Integer source;
    @NonNull
    private Integer destination;
    @NonNull
    private Short type;
    private String comment;
}
