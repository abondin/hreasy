package ru.abondin.hreasy.platform.service.admin.imp;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.abondin.hreasy.platform.I18Helper;
import ru.abondin.hreasy.platform.config.HrEasyCommonProps;
import ru.abondin.hreasy.platform.service.admin.imp.dto.ExcelImportContext;
import ru.abondin.hreasy.platform.service.admin.imp.dto.ExcelImportRowDto;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;


@Component
@Scope("prototype")
public class ExcelImportProcessorUtils {
    private final I18Helper i18n;
    private final HrEasyCommonProps props;

    private final DateTimeFormatter formatter;

    public ExcelImportProcessorUtils(I18Helper i18n, HrEasyCommonProps props) {
        this.i18n = i18n;
        this.props = props;
        this.formatter = DateTimeFormatter.ofPattern(props.getExcelImport().getDateFormat());
    }

    public <T, E> void apply(ExcelImportRowDto.DataProperty<T> prop,
                             Optional<E> existingEmployee,
                             Function<E, T> existingEmployeeProp,
                             Consumer<ExcelImportRowDto.DataProperty<T>> mapper) {
        prop.setCurrentValue(existingEmployee.map(existingEmployeeProp).orElse(null));
        if (prop.getRaw() != null) {
            mapper.accept(prop);
        }
    }

    public <E> void applyDict(ExcelImportRowDto.DataProperty<SimpleDictDto> prop,
                              ExcelImportContext<E> context,
                              List<SimpleDictDto> dict) {
        var value = prop.getRaw().trim().toLowerCase(context.locale());
        var key = dict
                .stream()
                .filter(d -> d.getName().toLowerCase(context.locale()).trim().equals(value))
                .findFirst();
        if (key.isPresent()) {
            prop.setImportedValue(key.get());
        } else {
            prop.setError(i18n.localize("errors.import.not_in_dict"));
        }
    }

    public void applyLocalDate(ExcelImportRowDto.DataProperty<LocalDate> prop) {
        try {
            var date = formatter.parse(prop.getRaw().trim(), LocalDate::from);
            prop.setImportedValue(date);
        } catch (DateTimeParseException ex) {
            prop.setError(i18n.localize("errors.import.invalid_date_format", props.getExcelImport().getDateFormat()));
        }
    }

    public void applyStringWithTrim(ExcelImportRowDto.DataProperty<String> prop) {
        prop.setImportedValue(prop.getRaw().trim());
    }

}
