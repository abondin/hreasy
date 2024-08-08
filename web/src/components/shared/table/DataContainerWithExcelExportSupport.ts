/**
 * TableComponentDataContainer must implements this interface to support HreasyTableExportToExcelAction toolbar button
 */
export interface DataContainerWithExcelExportSupport {
    canExport(): boolean;
    exportToExcel(): Promise<any>;
    get exportCompleted():boolean;
}
