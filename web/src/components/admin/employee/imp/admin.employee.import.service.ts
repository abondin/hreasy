import httpService from "../../../http.service";
import {AxiosInstance, AxiosResponse} from "axios";


/**
 * Import employees workflow with all configuration and data changes
 */
export interface ImportEmployeesWorkflow {
    id: number,
    newEmployeesCnt: number | null,
    updatedEmployeesCnt: number | null,
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
    config: EmployeeImportConfig | null,
    filename: string | null,
    data: ImportEmployeeExcelRows[]
}

export interface EmployeeImportConfig {
    sheetNumber: number,
    tableStartRow: number,
    columns: EmployeeImportConfigColumns
}

/**
 * 15 fields supported at the moment
 */
export interface EmployeeImportConfigColumns {
    displayName: string | null,
    externalErpId: string | null,
    email: string | null,
    phone: string | null,
    department: string | null,
    position: string | null,
    dateOfEmployment: string | null,
    dateOfDismissal: string | null,
    birthday: string | null,
    sex: string | null,
    documentSeries: string | null,
    documentNumber: string | null,
    documentIssuedDate: string | null,
    documentIssuedBy: string | null,
    registrationAddress: string | null
}

export interface ImportEmployeeExcelRows {
    email: string,
    employeeId: number | null,
    displayName: ExcelRowDataProperty<string>,
    externalErpId: ExcelRowDataProperty<string>,
    phone: ExcelRowDataProperty<string>,
    department: ExcelRowDataProperty<number>,
    position: ExcelRowDataProperty<number>,
    dateOfEmployment: ExcelRowDataProperty<string>,
    dateOfDismissal: ExcelRowDataProperty<string>,
    birthday: ExcelRowDataProperty<string>,
    sex: ExcelRowDataProperty<string>,
    documentSeries: ExcelRowDataProperty<string>,
    documentNumber: ExcelRowDataProperty<string>,
    documentIssuedDate: ExcelRowDataProperty<string>,
    documentIssuedBy: ExcelRowDataProperty<string>,
    registrationAddress: ExcelRowDataProperty<string>,
}

export interface ExcelRowDataProperty<T> {
    currentValue: T | null,
    importedValue: T | null,
    raw: string | null,
    error: string | null
}

export interface AdminEmployeeImportService {
    getActiveOrStartNewImportProcess(): Promise<ImportEmployeesWorkflow>;

    getUploadImportFileUrl(processId: number): string;

    applyConfigAndPreview(processId: number, config: EmployeeImportConfig): Promise<ImportEmployeesWorkflow>;

    commit(processId: number): Promise<ImportEmployeesWorkflow>;

    cancel(processId: number): Promise<ImportEmployeesWorkflow>;

}


class RestAdminEmployeeImportService implements AdminEmployeeImportService {
    constructor(private httpService: AxiosInstance) {
    }

    getActiveOrStartNewImportProcess(): Promise<ImportEmployeesWorkflow> {
        return httpService.post(`v1/admin/employees/import`)
            .then((response: AxiosResponse<ImportEmployeesWorkflow>) => {
                return response.data;
            });
    }

    getUploadImportFileUrl(processId: number): string {
        return `${httpService.defaults.baseURL}v1/admin/employees/import/${processId}/file`;
    }

    applyConfigAndPreview(processId: number, config: EmployeeImportConfig): Promise<ImportEmployeesWorkflow> {
        return httpService.post(`v1/admin/employees/import/${processId}/config`, config)
            .then((response: AxiosResponse<ImportEmployeesWorkflow>) => {
                return response.data;
            });
    }

    cancel(processId: number): Promise<ImportEmployeesWorkflow> {
        return httpService.post(`v1/admin/employees/import/${processId}/cancel`)
            .then((response: AxiosResponse<ImportEmployeesWorkflow>) => {
                return response.data;
            });
    }

    commit(processId: number): Promise<ImportEmployeesWorkflow> {
        return httpService.post(`v1/admin/employees/import/${processId}/commit`)
            .then((response: AxiosResponse<ImportEmployeesWorkflow>) => {
                return response.data;
            });
    }


}


const adminEmployeeImportService: AdminEmployeeImportService = new RestAdminEmployeeImportService(httpService);

export default adminEmployeeImportService;

