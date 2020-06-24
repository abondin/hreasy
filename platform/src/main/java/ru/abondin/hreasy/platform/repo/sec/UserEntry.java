package ru.abondin.hreasy.platform.repo.sec;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("sec_user")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEntry {
    @Id
    private Integer id;
    private String email;
    private Integer employeeId;
}
