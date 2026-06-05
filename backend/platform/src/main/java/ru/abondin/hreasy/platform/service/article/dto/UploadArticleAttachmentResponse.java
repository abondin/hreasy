package ru.abondin.hreasy.platform.service.article.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadArticleAttachmentResponse {
    private String relativeAttachmentUrl;
}
