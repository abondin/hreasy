import {
  type ExcelRowDataProperty,
  type ImportConfig,
  type ImportExcelRow,
  type ImportService,
  RestImportService,
} from "@/services/admin/import/import.base";

export interface EmployeeImportConfig extends ImportConfig {
  columns: EmployeeImportConfigColumns;
}

export interface EmployeeImportConfigColumns {
  [key: string]: string | null;
  displayName: string | null;
  externalErpId: string | null;
  email: string | null;
  phone: string | null;
  department: string | null;
  position: string | null;
  dateOfEmployment: string | null;
  dateOfDismissal: string | null;
  birthday: string | null;
  sex: string | null;
  documentSeries: string | null;
  documentNumber: string | null;
  documentIssuedDate: string | null;
  documentIssuedBy: string | null;
  registrationAddress: string | null;
}

export interface ImportEmployeeExcelRow extends ImportExcelRow {
  rowNumber: number;
  email: string;
  employeeId: number | null;
  displayName: ExcelRowDataProperty<string>;
  externalErpId: ExcelRowDataProperty<string>;
  phone: ExcelRowDataProperty<string>;
  organization?: ExcelRowDataProperty<{ id?: number; name?: string } | string | number>;
  department: ExcelRowDataProperty<number>;
  position: ExcelRowDataProperty<number>;
  dateOfEmployment: ExcelRowDataProperty<string>;
  dateOfDismissal: ExcelRowDataProperty<string>;
  birthday: ExcelRowDataProperty<string>;
  sex: ExcelRowDataProperty<string>;
  documentSeries: ExcelRowDataProperty<string>;
  documentNumber: ExcelRowDataProperty<string>;
  documentIssuedDate: ExcelRowDataProperty<string>;
  documentIssuedBy: ExcelRowDataProperty<string>;
  registrationAddress: ExcelRowDataProperty<string>;
}

export const adminEmployeeImportService: ImportService<
  EmployeeImportConfig,
  ImportEmployeeExcelRow
> = new RestImportService("v1/admin/employees/import");
