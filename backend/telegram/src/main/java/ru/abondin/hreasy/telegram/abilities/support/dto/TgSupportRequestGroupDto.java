package ru.abondin.hreasy.telegram.abilities.support.dto;

import lombok.Data;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@ToString(of={"key"})
public final class TgSupportRequestGroupDto implements Serializable {
    @Data
    public static final class TgSupportConfiguration implements Serializable {
        @Serial
        private static final long serialVersionUID = 0L;
        private List<String> categories;
    }

    @Serial
    private static final long serialVersionUID = 0L;
    private String key;
    private String displayName;
    private TgSupportConfiguration configuration;
}
