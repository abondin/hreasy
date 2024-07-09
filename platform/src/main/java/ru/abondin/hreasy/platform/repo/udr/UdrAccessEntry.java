package ru.abondin.hreasy.platform.repo.udr;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("udr.registry_access")
public class UdrAccessEntry {
    @Id
    private Integer id;

    @Column("registry_id")
    private Integer registryId;

    @Column("employee_id")
    private Integer employeeId;

    @Column("read_permission")
    private Boolean readPermission = false;

    @Column("write_permission")
    private Boolean writePermission = false;
}
