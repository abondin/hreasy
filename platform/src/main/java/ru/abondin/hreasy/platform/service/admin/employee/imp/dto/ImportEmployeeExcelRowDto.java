package ru.abondin.hreasy.platform.service.admin.employee.imp.dto;


import lombok.Data;
import ru.abondin.hreasy.platform.service.admin.imp.dto.ExcelImportRowDto;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

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
public class ImportEmployeeExcelRowDto extends ExcelImportRowDto {
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
    private DataProperty<SimpleDictDto> department = new DataProperty<>();
    private DataProperty<SimpleDictDto> position = new DataProperty<>();
    private DataProperty<LocalDate> dateOfEmployment = new DataProperty<>();
    private DataProperty<LocalDate> dateOfDismissal = new DataProperty<>();
    private DataProperty<LocalDate> birthday = new DataProperty<>();
    private DataProperty<String> sex = new DataProperty<>();
    private DataProperty<String> documentSeries = new DataProperty<>();
    private DataProperty<String> documentNumber = new DataProperty<>();
    private DataProperty<LocalDate> documentIssuedDate = new DataProperty<>();
    private DataProperty<String> documentIssuedBy = new DataProperty<>();
    private DataProperty<String> registrationAddress = new DataProperty<>();
    private DataProperty<SimpleDictDto> organization = new DataProperty<>();

    @Override
    public boolean isNew() {
        return employeeId == null;
    }

    @Override
    protected Stream<DataProperty<?>> allProperties() {
        return Stream.of(
                displayName,
                externalErpId,
                phone,
                department,
                organization,
                position,
                dateOfEmployment,
                dateOfDismissal,
                birthday,
                sex,
                documentSeries,
                documentNumber,
                documentIssuedDate,
                documentIssuedBy,
                registrationAddress
        );
    }

}
