import DOMPurify from "dompurify";
import http from "@/lib/http";
import type { Employee } from "@/services/employee.service";
import { listEmployees } from "@/services/employee.service";

export interface OfficeMapData {
  svg: string;
  employees: Employee[];
}

export async function fetchOfficeMapSvg(filename: string): Promise<string> {
  const response = await http.get<string>(`v1/dict/office_maps/${filename}`, {
    responseType: "text",
  });
  return DOMPurify.sanitize(response.data, {
    USE_PROFILES: { svg: true },
  }) as string;
}

export async function loadOfficeMapData(filename: string): Promise<OfficeMapData> {
  const [svg, employees] = await Promise.all([
    fetchOfficeMapSvg(filename),
    listEmployees(),
  ]);

  const employeesForMap = employees.filter(
    (employee) => employee.officeLocation?.mapName === filename,
  );

  return {
    svg,
    employees: employeesForMap,
  };
}
