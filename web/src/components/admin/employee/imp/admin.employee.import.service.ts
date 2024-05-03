import httpService from "../../../http.service";
import {
    ExcelRowDataProperty,
    ImportConfig,
    ImportExcelRow,
    ImportService,
    RestImportService
} from "@/components/admin/imp/import.base";


export interface EmployeeImportConfig extends ImportConfig {
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

export interface ImportEmployeeExcelRows extends ImportExcelRow {
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
    registrationAddress: ExcelRowDataProperty<string>
}

const adminEmployeeImportService: ImportService<EmployeeImportConfig, ImportEmployeeExcelRows>
    = new RestImportService<EmployeeImportConfig, ImportEmployeeExcelRows>(httpService, 'v1/admin/employees/import');

export default adminEmployeeImportService;

