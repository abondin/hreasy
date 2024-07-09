package ru.abondin.hreasy.platform.repo.udr;

import io.r2dbc.postgresql.codec.Json;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Data
@Table("udr.registry_employee")
public class UdrEmployee {
    @Id
    private Integer id;

    @Column("employee_id")
    private Integer employeeId;

    private Boolean active = true;

    @Column("custom_fields_values")
    private Json customFieldsValues;
}

