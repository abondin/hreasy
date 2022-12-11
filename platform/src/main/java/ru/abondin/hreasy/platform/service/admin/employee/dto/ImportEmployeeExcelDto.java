package ru.abondin.hreasy.platform.service.admin.employee.dto;


import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
/**
 * Collection of all imported attributes.
 * Each attribute contains current value from database,
 * display name from database,
 * raw imported value,
 * processed imported value (find dict key by dict value)
 * <p>
 *     14 fields can be imported
 * </p>
 */
@Data
public class ImportEmployeeExcelDto {
    public static class DataProperty<T> {
        private T currentValue;
        private T importedValue;
        private String raw;
    }

    private DataProperty<String> displayName = new DataProperty<>();
    private DataProperty<String> email = new DataProperty<>();
    private DataProperty<String> phone = new DataProperty<>();
    private DataProperty<String> departmentName = new DataProperty<>();
    private DataProperty<String> positionName = new DataProperty<>();
    private DataProperty<String> dateOfEmployment = new DataProperty<>();
    private DataProperty<String> dateOfDismissal = new DataProperty<>();
    private DataProperty<String> birthday = new DataProperty<>();
    private DataProperty<String> sex = new DataProperty<>();
    private DataProperty<String> documentSeries = new DataProperty<>();
    private DataProperty<String> documentNumber = new DataProperty<>();
    private DataProperty<String> documentIssuedDate = new DataProperty<>();
    private DataProperty<String> documentIssuedBy = new DataProperty<>();
    private DataProperty<String> registrationAddress = new DataProperty<>();
}
