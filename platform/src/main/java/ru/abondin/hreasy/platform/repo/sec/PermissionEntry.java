package ru.abondin.hreasy.platform.repo.sec;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("role_perm")
@Data
public class PermissionEntry {
    @Id
    private Integer id;
    private String permission;
}
