import httpService from "../../../http.service";
import {
    ExcelRowDataProperty,
    ImportConfig,
    ImportExcelRow,
    ImportService,
    RestImportService
} from "@/components/admin/imp/import.base";
import {SimpleDict} from "@/store/modules/dict";

export interface EmployeeKidImportConfig extends ImportConfig {
    columns: EmployeeKidImportConfigColumns
}

export interface EmployeeKidImportConfigColumns {
    displayName: string | null,
    birthday: string | null,
    parentEmail: string | null,
}

export interface ImportEmployeeKidExcelRows extends ImportExcelRow {
    employeeKidId: number | null,
    displayName: string,
    parentEmail: string,
    parent: SimpleDict,
    birthday: ExcelRowDataProperty<string>,
}


const adminEmployeeKidImportService: ImportService<EmployeeKidImportConfig, ImportEmployeeKidExcelRows>
    = new RestImportService<EmployeeKidImportConfig, ImportEmployeeKidExcelRows>(httpService, 'v1/admin/employees/kids/import');

export default adminEmployeeKidImportService;

