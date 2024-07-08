package ru.abondin.hreasy.telegram.abilities.support;

import lombok.Data;
import lombok.ToString;
import ru.abondin.hreasy.telegram.abilities.support.dto.TgSupportRequestGroupDto;

import java.io.Serial;
import java.io.Serializable;

@Data
@ToString(of = {"group", "category"})
public class SupportRequestSession implements Serializable {
    @Serial
    private static final long serialVersionUID = 0L;

    protected SupportRequestSession() {

    }


    private TgSupportRequestGroupDto group;
    private String category;
}
