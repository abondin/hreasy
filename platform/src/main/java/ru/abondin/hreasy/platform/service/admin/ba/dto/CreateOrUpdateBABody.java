package ru.abondin.hreasy.platform.service.admin.ba.dto;

import lombok.Data;
import lombok.ToString;
import org.springframework.lang.Nullable;

import org.springframework.lang.NonNull;

@Data
@ToString
public class CreateOrUpdateBABody {
    @NonNull
    private String name;
    @Nullable
    private String description;

    private boolean archived = false;

}
