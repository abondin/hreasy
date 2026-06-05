package ru.abondin.hreasy.platform.service.techprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Schema(description = "Technical profile of employee")
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeTechProfileFileDto {
    @Schema(description = "Name of the uploaded file", required = true, example = "Alexander Bondin Java 2022.pdf")
    private String filename;
    @Schema(description = "Temporary access token to download file")
    private String accessToken;
}
