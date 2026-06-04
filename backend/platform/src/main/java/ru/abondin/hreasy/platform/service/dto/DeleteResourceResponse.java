package ru.abondin.hreasy.platform.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * For example when delete attachment or location map
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteResourceResponse {
    private boolean deleted = true;
}
