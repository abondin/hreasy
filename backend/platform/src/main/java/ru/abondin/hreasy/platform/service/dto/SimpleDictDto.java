package ru.abondin.hreasy.platform.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
@Schema(description = "Reference to an HR Easy dictionary entity.")
public class SimpleDictDto{
    @Schema(description = "HR Easy identifier of the referenced entity.", example = "301")
    private int id;
    @Schema(description = "Display name of the referenced entity.", example = "Example project")
    private String name;
    /**
     * false if element should be hidden or UI by default
     */
    @Schema(description = "Whether the entity should be shown as active by default.")
    private boolean active = true;

    public SimpleDictDto(int id, String name) {
        this(id, name, true);
    }

    public SimpleDictDto(int id, String name, boolean active) {
        this.id = id;
        this.name = name;
        this.active = active;
    }

}
