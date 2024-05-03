package ru.abondin.hreasy.platform.service.admin.employee.kids.imp.dto;


import lombok.Data;
import ru.abondin.hreasy.platform.service.admin.imp.dto.ExcelImportRowDto;

import java.time.LocalDate;
import java.util.stream.Stream;

/**
 * Collection of all imported attributes.
 * Each attribute contains current value from database,
 * display name from database,
 * raw imported value,
 * processed imported value (find dict key by dict value)
 * <p>
 * 15 fields can be imported
 * </p>
 */
@Data
public class ImportEmployeeKidExcelRowDto extends ExcelImportRowDto {
    /**
     * Key properties to merge employees from excel and database
     */
    private String displayName;
    private String parentEmail;
    /**
     * If kid found in database
     */
    private Integer employeeKidId;
    private Integer parentId;

    private DataProperty<LocalDate> birthday = new DataProperty<>();

    @Override
    public boolean isNew() {
        return employeeKidId == null;
    }

    @Override
    protected Stream<DataProperty<?>> allProperties() {
        return Stream.of(
                birthday
        );
    }

}
