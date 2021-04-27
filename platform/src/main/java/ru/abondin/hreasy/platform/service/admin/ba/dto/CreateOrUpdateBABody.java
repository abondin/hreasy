package ru.abondin.hreasy.platform.service.admin.ba.dto;

import lombok.Data;
import lombok.ToString;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Data
@ToString
public class CreateOrUpdateBABody {
    @NotNull
    private String name;
    @Nullable
    private Integer responsibleEmployee;
    @Nullable
    private String description;

}
