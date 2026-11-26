# Excel reports

`ExcelReportFactory` creates branded POI workbooks. Employee exports use it;
other reports retain their existing exporters.

## Report ownership

- The exporter selects columns and data, applies permissions and filters, and sorts rows.
- `ExcelReportFactory.Column` defines an i18n header key, width in characters, and value extractor.
- `Report.addSheet` writes a title, one metadata line, and an Excel table with filters.
- `ExcelReportStyler` applies workbook-local styles and places the logo on each sheet.
- The title and metadata value span the table width in merged cells; both top rows use the branding background.
- Create a report with `create(locale)`, add sheets, write it to an output stream, and close it with try-with-resources.

Text, numbers, booleans, and `LocalDate` values are supported. Dates remain native Excel dates;
the bundled branding displays them as `dd.mm.yyyy`.
Sheet names, report titles, metadata labels, and column headers use backend i18n keys.
Table names are unique Excel identifiers supplied by the exporter.

## Branding

Edit `backend/platform/src/main/resources/excel/excel-branding.xlsx` and rebuild the application.
The workbook contains formatting samples, not employee data or report columns.
Keep these workbook-level names pointing to individual cells:

| Name | Purpose |
| --- | --- |
| `ReportTitle` | Title font and style; its row height controls the title row |
| `MetadataLabel` | Metadata label style |
| `MetadataValue` | Metadata value style |
| `ColumnHeader` | Column header style and row height |
| `TextCell` | Text and empty-cell style |
| `NumberCell` | Numeric style |
| `DateCell` | Date style and full-date number format |
| `TableStyle` | Cell text containing the built-in Excel table style name |

The first picture on the `ReportTitle` sheet is the logo. Its anchor and crop are copied
to each report sheet. Keep it within column A and the first two rows; the title starts in B1.
Use explicit colors for brand-specific styling. Styles are copied once per workbook and
reused across sheets and rows. Formatting sample values are never copied into reports.
