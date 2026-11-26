package ru.abondin.hreasy.platform.service.excel;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import ru.abondin.hreasy.platform.I18Helper;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import static ru.abondin.hreasy.platform.service.excel.ExcelReportConstants.*;

/** Creates branded Excel reports; callers own data selection, ordering and i18n keys. */
@Component
@RequiredArgsConstructor
public class ExcelReportFactory {
    private final I18Helper i18Helper;

    public record Column<T>(String titleKey, int width, Function<T, ?> value) {}

    /** Creates an independent workbook. The caller must close the report. */
    public Report create(Locale locale) throws IOException {
        var workbook = new XSSFWorkbook();
        try (var input = new ClassPathResource(BRANDING_RESOURCE).getInputStream();
             var branding = new XSSFWorkbook(input)) {
            return new Report(workbook, new ExcelReportStyler(workbook, branding), locale);
        } catch (IOException | RuntimeException e) {
            workbook.close();
            throw e;
        }
    }

    /** Mutable report scoped to one export; no workbook or cell styles are shared across requests. */
    public final class Report implements AutoCloseable {
        private final XSSFWorkbook workbook;
        private final ExcelReportStyler styler;
        private final Locale locale;

        private Report(XSSFWorkbook workbook, ExcelReportStyler styler, Locale locale) {
            this.workbook = workbook;
            this.styler = styler;
            this.locale = locale;
        }

        /** Adds a branded table sheet. Titles are i18n keys; metadataValue is already formatted. */
        public <T> void addSheet(String sheetNameKey, String tableName, String titleKey,
                                 String metadataLabelKey, String metadataValue,
                                 List<Column<T>> columns, List<T> items) {
            if (columns.isEmpty()) {
                throw new IllegalArgumentException("An Excel table requires at least one column");
            }
            var sheet = workbook.createSheet(i18Helper.localize(locale, sheetNameKey));
            var title = sheet.createRow(TITLE_ROW).createCell(TITLE_COLUMN);
            title.setCellValue(i18Helper.localize(locale, titleKey));
            title.setCellStyle(styler.style(REPORT_TITLE));
            var metadata = sheet.createRow(METADATA_ROW);
            var label = metadata.createCell(METADATA_LABEL_COLUMN);
            label.setCellValue(i18Helper.localize(locale, metadataLabelKey));
            label.setCellStyle(styler.style(METADATA_LABEL));
            var valueCell = metadata.createCell(METADATA_VALUE_COLUMN);
            valueCell.setCellValue(metadataValue);
            valueCell.setCellStyle(styler.style(METADATA_VALUE));
            var header = sheet.createRow(HEADER_ROW);
            for (int i = 0; i < columns.size(); i++) {
                var column = columns.get(i);
                sheet.setColumnWidth(i, column.width() * COLUMN_WIDTH_UNIT);
                var cell = header.createCell(i);
                cell.setCellValue(i18Helper.localize(locale, column.titleKey()));
                cell.setCellStyle(styler.style(COLUMN_HEADER));
            }
            for (var item : items) {
                var row = sheet.createRow(sheet.getLastRowNum() + 1);
                for (int i = 0; i < columns.size(); i++) {
                    var cell = row.createCell(i);
                    cell.setCellStyle(styler.style(TEXT_CELL));
                    var value = columns.get(i).value().apply(item);
                    switch (value) {
                        case null -> { }
                        case LocalDate date -> {
                            cell.setCellValue(date);
                            cell.setCellStyle(styler.style(DATE_CELL));
                        }
                        case Number number -> {
                            cell.setCellValue(number.doubleValue());
                            cell.setCellStyle(styler.style(NUMBER_CELL));
                        }
                        case Boolean bool -> cell.setCellValue(bool);
                        default -> cell.setCellValue(value.toString());
                    }
                }
            }
            // Excel tables require a data row even when the result is empty.
            if (items.isEmpty()) {
                sheet.createRow(FIRST_DATA_ROW);
            }
            var table = sheet.createTable(new AreaReference(new CellReference(HEADER_ROW, 0),
                    new CellReference(sheet.getLastRowNum(), columns.size() - 1), SpreadsheetVersion.EXCEL2007));
            table.setName(tableName);
            table.setDisplayName(tableName);
            table.getCTTable().addNewAutoFilter().setRef(table.getCTTable().getRef());
            styler.decorateTable(table);
            styler.decorateSheet(sheet, columns.size());
        }

        public void write(OutputStream output) throws IOException {
            workbook.write(output);
        }

        @Override
        public void close() throws IOException {
            workbook.close();
        }
    }
}
