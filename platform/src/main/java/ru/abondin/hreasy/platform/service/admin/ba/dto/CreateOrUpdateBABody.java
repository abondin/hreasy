package ru.abondin.hreasy.platform.service.admin.ba.dto;

import lombok.Data;
import lombok.ToString;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;

@Data
@ToString
public class CreateOrUpdateBABody {
    @NotNull
    private String name;
    @Nullable
    private String description;

    private boolean archived = false;

}
