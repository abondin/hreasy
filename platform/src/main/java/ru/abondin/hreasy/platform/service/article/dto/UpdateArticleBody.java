package ru.abondin.hreasy.platform.service.article.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

/**
 * Information about article
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateArticleBody {
    @NotNull
    private String name;
    @NotNull
    private String articleGroup;
    @Nullable
    private String description;
    @Nullable
    private String content;
    private boolean moderated = true;
    private boolean archived = false;
}
