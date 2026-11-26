package ru.abondin.hreasy.platform.service.excel;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.abondin.hreasy.platform.service.excel.ExcelReportConstants.*;

/** Per-workbook styles and logo copied from named samples in the branding workbook. */
final class ExcelReportStyler {
    private final Map<String, CellStyle> styles;
    private final String tableStyle;
    private final float titleHeight;
    private final float headerHeight;
    private final XSSFPicture logo;
    private final int pictureIndex;

    ExcelReportStyler(XSSFWorkbook workbook, XSSFWorkbook branding) {
        styles = Stream.of(REPORT_TITLE, METADATA_LABEL, METADATA_VALUE, COLUMN_HEADER,
                        TEXT_CELL, NUMBER_CELL, DATE_CELL)
                .collect(Collectors.toMap(name -> name, name -> {
                    var style = workbook.createCellStyle();
                    style.cloneStyleFrom(sample(branding, name).getCellStyle());
                    return style;
                }));
        tableStyle = sample(branding, TABLE_STYLE).getStringCellValue();
        titleHeight = sample(branding, REPORT_TITLE).getRow().getHeightInPoints();
        headerHeight = sample(branding, COLUMN_HEADER).getRow().getHeightInPoints();
        logo = sample(branding, REPORT_TITLE).getSheet().getDrawingPatriarch().getShapes().stream()
                .filter(XSSFPicture.class::isInstance).map(XSSFPicture.class::cast).findFirst().orElseThrow();
        pictureIndex = workbook.addPicture(logo.getPictureData().getData(), logo.getPictureData().getPictureType());
    }

    private XSSFCell sample(XSSFWorkbook workbook, String name) {
        var namedCell = workbook.getName(name);
        if (namedCell == null) {
            throw new IllegalArgumentException("Missing Excel branding sample: " + name);
        }
        var reference = new CellReference(namedCell.getRefersToFormula());
        return workbook.getSheet(reference.getSheetName()).getRow(reference.getRow()).getCell(reference.getCol());
    }

    CellStyle style(String name) {
        return styles.get(name);
    }

    void decorateSheet(XSSFSheet sheet, int columnCount) {
        sheet.getRow(TITLE_ROW).setHeightInPoints(titleHeight);
        sheet.getRow(HEADER_ROW).setHeightInPoints(headerHeight);
        int lastColumn = Math.max(METADATA_VALUE_COLUMN, columnCount - 1);
        for (int rowIndex = TITLE_ROW; rowIndex < HEADER_ROW; rowIndex++) {
            var row = sheet.getRow(rowIndex);
            for (int column = 0; column <= lastColumn; column++) {
                if (row.getCell(column) == null) {
                    row.createCell(column).setCellStyle(style(rowIndex == TITLE_ROW ? REPORT_TITLE : METADATA_VALUE));
                }
            }
        }
        sheet.addMergedRegion(new CellRangeAddress(TITLE_ROW, TITLE_ROW, TITLE_COLUMN, lastColumn));
        if (lastColumn > METADATA_VALUE_COLUMN) {
            sheet.addMergedRegion(new CellRangeAddress(METADATA_ROW, METADATA_ROW, METADATA_VALUE_COLUMN, lastColumn));
        }
        var original = logo.getClientAnchor();
        var anchor = new XSSFClientAnchor(original.getDx1(), original.getDy1(), original.getDx2(), original.getDy2(),
                original.getCol1(), original.getRow1(), original.getCol2(), original.getRow2());
        anchor.setAnchorType(original.getAnchorType());
        var picture = sheet.createDrawingPatriarch().createPicture(anchor, pictureIndex);
        if (logo.getCTPicture().getBlipFill().isSetSrcRect()) {
            picture.getCTPicture().getBlipFill().setSrcRect(logo.getCTPicture().getBlipFill().getSrcRect());
        }
    }

    void decorateTable(XSSFTable table) {
        table.setStyleName(tableStyle);
        table.getCTTable().getTableStyleInfo().setShowRowStripes(true);
    }
}
