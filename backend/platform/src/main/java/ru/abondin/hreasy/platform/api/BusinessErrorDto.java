.package ru.abondin.hreasy.platform.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Standard platform business or access error response.")
public class BusinessErrorDto {
    @Schema(description = "Machine-readable error code.", example = "errors.access.denied")
    private String code;
    @Schema(description = "Localized message for the caller.")
    private String message;
    @Schema(description = "Additional error attributes; values depend on the error code.")
    private Map<String, Object> args = new HashMap<>();
}
