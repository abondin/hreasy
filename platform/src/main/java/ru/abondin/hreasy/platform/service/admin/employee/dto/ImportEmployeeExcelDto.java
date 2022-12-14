package ru.abondin.hreasy.platform.service.admin.employee.dto;


import lombok.Data;

import java.time.LocalDate;

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
public class ImportEmployeeExcelDto {
    /**
     * Key property to merge employees from excel and database
     */
    private String email;
    /**
     * If employee found in database
     */
    private Integer employeeId;
    private DataProperty<String> displayName = new DataProperty<>();
    private DataProperty<String> externalErpId = new DataProperty<>();
    private DataProperty<String> phone = new DataProperty<>();
    private DataProperty<Integer> department = new DataProperty<>();
    private DataProperty<Integer> position = new DataProperty<>();
    private DataProperty<LocalDate> dateOfEmployment = new DataProperty<>();
    private DataProperty<LocalDate> dateOfDismissal = new DataProperty<>();
    private DataProperty<LocalDate> birthday = new DataProperty<>();
    private DataProperty<String> sex = new DataProperty<>();
    private DataProperty<String> documentSeries = new DataProperty<>();
    private DataProperty<String> documentNumber = new DataProperty<>();
    private DataProperty<LocalDate> documentIssuedDate = new DataProperty<>();
    private DataProperty<String> documentIssuedBy = new DataProperty<>();
    private DataProperty<String> registrationAddress = new DataProperty<>();

    @Data
    public static class DataProperty<T> {
        private T currentValue;
        private T importedValue;
        private String raw;
        private String error;
    }

}
