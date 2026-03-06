import http from "@/lib/http";

export interface ImportConfig {
  sheetNumber: number;
  tableStartRow: number;
}

export interface ImportExcelRow {
  new: boolean;
  errorCount: number;
  updatedCellsCount: number;
}

export interface ImportWorkflow<C extends ImportConfig, R extends ImportExcelRow> {
  id: number;
  state: number;
  config: C | null;
  filename: string | null;
  importedRows: R[];
  importProcessStats: ImportProcessStats;
}

export interface ExcelRowDataProperty<T> {
  currentValue: T | null;
  importedValue: T | null;
  raw: string | null;
  error: string | null;
  updated: boolean;
}

export interface ImportProcessStats {
  processedRows: number;
  errors: number;
  newItems: number;
  updatedItems: number;
}

export interface ImportService<C extends ImportConfig, R extends ImportExcelRow> {
  getActiveOrStartNewImportProcess(): Promise<ImportWorkflow<C, R>>;
  getUploadImportFileUrl(processId: number): string;
  uploadFile(processId: number, file: File): Promise<void>;
  applyConfigAndPreview(processId: number, config: C): Promise<ImportWorkflow<C, R>>;
  commit(processId: number): Promise<ImportWorkflow<C, R>>;
  cancel(processId: number): Promise<ImportWorkflow<C, R>>;
}

export class RestImportService<C extends ImportConfig, R extends ImportExcelRow>
  implements ImportService<C, R>
{
  constructor(private readonly baseUrl: string) {}

  async getActiveOrStartNewImportProcess(): Promise<ImportWorkflow<C, R>> {
    const response = await http.post<ImportWorkflow<C, R>>(this.baseUrl);
    return response.data;
  }

  getUploadImportFileUrl(processId: number): string {
    const baseURL = normalizeBaseUrl(http.defaults.baseURL);
    return `${baseURL}${this.baseUrl}/${processId}/file`;
  }

  async uploadFile(processId: number, file: File): Promise<void> {
    const formData = new FormData();
    formData.append("file", file, file.name);
    await http.post(`${this.baseUrl}/${processId}/file`, formData);
  }

  async applyConfigAndPreview(
    processId: number,
    config: C,
  ): Promise<ImportWorkflow<C, R>> {
    const response = await http.post<ImportWorkflow<C, R>>(
      `${this.baseUrl}/${processId}/config`,
      config,
    );
    return response.data;
  }

  async cancel(processId: number): Promise<ImportWorkflow<C, R>> {
    const response = await http.post<ImportWorkflow<C, R>>(
      `${this.baseUrl}/${processId}/cancel`,
    );
    return response.data;
  }

  async commit(processId: number): Promise<ImportWorkflow<C, R>> {
    const response = await http.post<ImportWorkflow<C, R>>(
      `${this.baseUrl}/${processId}/commit`,
    );
    return response.data;
  }
}

function normalizeBaseUrl(baseUrl: string | undefined): string {
  const value = baseUrl ?? "/api/";
  const normalized = value.endsWith("/") ? value : `${value}/`;
  return normalized.startsWith("http") ? normalized : `${window.location.origin}${normalized}`;
}
