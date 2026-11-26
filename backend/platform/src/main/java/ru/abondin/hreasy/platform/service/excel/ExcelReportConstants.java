package ru.abondin.hreasy.platform.service.excel;

/** Branding sample names and zero-based layout coordinates shared by Excel reports. */
public final class ExcelReportConstants {
    public static final String BRANDING_RESOURCE = "excel/excel-branding.xlsx";

    public static final String REPORT_TITLE = "ReportTitle";
    public static final String METADATA_LABEL = "MetadataLabel";
    public static final String METADATA_VALUE = "MetadataValue";
    public static final String COLUMN_HEADER = "ColumnHeader";
    public static final String TEXT_CELL = "TextCell";
    public static final String NUMBER_CELL = "NumberCell";
    public static final String DATE_CELL = "DateCell";
    public static final String TABLE_STYLE = "TableStyle";

    public static final int TITLE_ROW = 0;
    public static final int METADATA_ROW = 1;
    public static final int HEADER_ROW = 2;
    public static final int FIRST_DATA_ROW = 3;
    public static final int TITLE_COLUMN = 1;
    public static final int METADATA_LABEL_COLUMN = 1;
    public static final int METADATA_VALUE_COLUMN = 2;
    public static final int COLUMN_WIDTH_UNIT = 256;

    private ExcelReportConstants() {
    }
}
