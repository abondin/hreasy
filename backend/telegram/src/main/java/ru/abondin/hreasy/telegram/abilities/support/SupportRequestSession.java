package ru.abondin.hreasy.telegram.abilities.support;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.ToString;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import ru.abondin.hreasy.telegram.abilities.support.dto.TgSupportRequestGroupDto;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@ToString(of = {"group", "category"})
public class SupportRequestSession implements Serializable {
    @Serial
    private static final long serialVersionUID = 0L;
    /**
     * Telegram API allows only 64 bytes in callback data.
     * This is why we have to cache categories
     */
    private final List<Tuple2<String, String>> categoriesCache = new ArrayList<>();

    protected SupportRequestSession() {
    }

    @Setter(AccessLevel.PRIVATE)
    private TgSupportRequestGroupDto group;
    private String categoryUuid;

    public void setGroup(TgSupportRequestGroupDto group, String defaultCategoryName) {
        this.group = group;
        this.categoryUuid = null;
        this.categoriesCache.clear();
        if (this.group != null && this.group.getConfiguration() != null && !this.group.getConfiguration().getCategories().isEmpty()) {
            this.categoriesCache.addAll(
                    this.group.getConfiguration().getCategories().stream().map(c -> Tuples.of(UUID.randomUUID().toString(), c)).toList()
            );
        }
        this.categoriesCache.add(Tuples.of("other", defaultCategoryName));
    }

    public String getCategory() {
        return this.categoriesCache.stream().filter(t -> t.getT1().equals(this.categoryUuid)).findFirst().orElseThrow().getT2();
    }
}
