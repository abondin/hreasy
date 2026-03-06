import type { Dict } from "@/services/employee.service";
import {
  type ExcelRowDataProperty,
  type ImportConfig,
  type ImportExcelRow,
  type ImportService,
  RestImportService,
} from "@/services/admin/import/import.base";

export interface EmployeeKidImportConfig extends ImportConfig {
  columns: EmployeeKidImportConfigColumns;
}

export interface EmployeeKidImportConfigColumns {
  displayName: string | null;
  birthday: string | null;
  parentEmail: string | null;
}

export interface ImportEmployeeKidExcelRow extends ImportExcelRow {
  employeeKidId: number | null;
  displayName: string;
  parentEmail: string;
  parent: Dict;
  birthday: ExcelRowDataProperty<string>;
}

export const adminEmployeeKidImportService: ImportService<
  EmployeeKidImportConfig,
  ImportEmployeeKidExcelRow
> = new RestImportService("v1/admin/employees/kids/import");
