package ru.abondin.hreasy.platform.service.admin.ba.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.lang.Nullable;

import org.springframework.lang.NonNull;

@Data
@NoArgsConstructor
@ToString
public class CreateOrUpdateBABody {
    @NonNull
    private String name;
    @Nullable
    private String description;

    private boolean archived = false;

}
