//
// base functionality for import from excel
//


import {
    EmployeeImportConfig,
    ImportEmployeeExcelRows
} from "@/components/admin/employee/imp/admin.employee.import.service";
import {AxiosInstance, AxiosResponse} from "axios";
import httpService from "@/components/http.service";

/**
 * How to parse excel
 */
export interface ImportConfig {
    sheetNumber: number,
    tableStartRow: number,
}

/**
 * Data of one excel row
 */
export interface ImportExcelRow {
    new: boolean,
    errorCount: number,
    updatedCellsCount: number
}


/**
 * Import workflow with all configuration and data changes
 */
export interface ImportWorkflow<C extends ImportConfig, R extends ImportExcelRow> {
    id: number,
    /**
     * <ul>
     *     <li>0 - created</li>
     *     <li>1 - file uploaded</li>
     *     <li>2 - configuration set</li>
     *     <li>3 - changes applied</li>
     *     <li>-1 - aborted</>
     * </ul>
     */
    state: number,
    config: C | null,
    filename: string | null,
    importedRows: R[],
    importProcessStats: ImportProcessStats
}

export interface ExcelRowDataProperty<T> {
    currentValue: T | null,
    importedValue: T | null,
    raw: string | null,
    error: string | null,
    updated: false
}

export interface ImportProcessStats {
    processedRows: number,
    errors: number,
    newItems: number,
    updatedItems: number
}

export interface ImportService<C extends ImportConfig, R extends ImportExcelRow> {
    getActiveOrStartNewImportProcess(): Promise<ImportWorkflow<C, R>>;

    getUploadImportFileUrl(processId: number): string;

    applyConfigAndPreview(processId: number, config: C): Promise<ImportWorkflow<C, R>>;

    commit(processId: number): Promise<ImportWorkflow<C, R>>;

    cancel(processId: number): Promise<ImportWorkflow<C, R>>;
}

export class RestImportService<C extends ImportConfig, R extends ImportExcelRow> implements ImportService<C, R> {
    constructor(private httpService: AxiosInstance, private baseUrl: string) {
    }

    getActiveOrStartNewImportProcess(): Promise<ImportWorkflow<C, R>> {
        return httpService.post(`${this.baseUrl}`)
            .then((response: AxiosResponse<ImportWorkflow<C, R>>) => {
                return response.data;
            });
    }

    getUploadImportFileUrl(processId: number): string {
        return `${httpService.defaults.baseURL}${this.baseUrl}/${processId}/file`;
    }

    applyConfigAndPreview(processId: number, config: C): Promise<ImportWorkflow<C, R>> {
        return httpService.post(`${this.baseUrl}/${processId}/config`, config)
            .then((response: AxiosResponse<ImportWorkflow<C, R>>) => {
                return response.data;
            });
    }

    cancel(processId: number): Promise<ImportWorkflow<C, R>> {
        return httpService.post(`${this.baseUrl}/${processId}/cancel`)
            .then((response: AxiosResponse<ImportWorkflow<C, R>>) => {
                return response.data;
            });
    }

    commit(processId: number): Promise<ImportWorkflow<C, R>> {
        return httpService.post(`${this.baseUrl}/${processId}/commit`)
            .then((response: AxiosResponse<ImportWorkflow<C, R>>) => {
                return response.data;
            });
    }
}
