package ru.abondin.hreasy.platform.repo.ba;

import io.r2dbc.postgresql.codec.Json;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

/**
 * Business account that generate a profit
 */
@Data
@Table("ba.business_account")
public class BusinessAccountEntry {
    @Id
    private Integer id;
    private String name;
    private String description;
    /**
     * Responsible employees array with additional meta. JSON <b>must be</b> compatible with {@link ru.abondin.hreasy.platform.service.ba.dto.BusinessAccountResponsibleEmployeeDto}
     *
     * <p><b color="red">WARNING:</b> Display Name of the employee does not update automatically when employee profile changing</p>
     *
     * @see ru.abondin.hreasy.platform.service.ba.dto.BusinessAccountResponsibleEmployeeDto
     */
    private Json responsibleEmployees;
    private boolean archived;
    private OffsetDateTime createdAt;
    private Integer createdBy;
}
