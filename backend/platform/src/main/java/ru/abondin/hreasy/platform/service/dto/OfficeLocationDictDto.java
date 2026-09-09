package ru.abondin.hreasy.platform.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

/**
 * Simple dict for the office location with link to office
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Schema(description = "Office location reference, with its parent office and map.")
public class OfficeLocationDictDto extends SimpleDictDto {
    @Nullable
    @Schema(description = "HR Easy identifier of the parent office.", nullable = true)
    private Integer officeId;
    @Schema(description = "Office map name.", nullable = true)
    private String mapName;

    public OfficeLocationDictDto(Integer id, String name, Integer officeId, String mapName) {
        super(id, name);
        this.officeId = officeId;
        this.mapName = mapName;
    }
}
