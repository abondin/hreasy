import type { Employee } from "@/services/employee.service";
import { loadOfficeMapData } from "@/services/office-map.service";
import { errorUtils } from "@/lib/errors";

export type MapReadyListener = () => void | Promise<void>;

export class MapPreviewData {
  opened = false;
  loading = false;
  error = "";
  img: string | null = null;
  filename: string | null = null;
  mapReadyListener: MapReadyListener | null = null;
  employees: Employee[] = [];
  highlightedWorkplace: string[] = [];
  title: string | null = null;
  employeeDetailedOpened = false;
  selectedEmployee: Employee | null = null;

  async show(
    filename: string,
    title?: string | null,
    highlightedWorkplace?: string[],
  ): Promise<void> {
    this.reset();
    this.filename = filename;
    this.title = title ?? null;
    this.highlightedWorkplace = highlightedWorkplace ?? [];
    this.opened = true;
    this.loading = true;
    try {
      const { svg, employees } = await loadOfficeMapData(filename);
      this.img = svg;
      this.employees = employees;
    } catch (error) {
      this.error = errorUtils.shortMessage(error);
    } finally {
      this.loading = false;
    }

    if (this.mapReadyListener) {
      await this.mapReadyListener();
    }
  }

  hide(): void {
    this.opened = false;
    this.img = null;
  }

  openEmployeeDetails(employee: Employee): void {
    this.selectedEmployee = employee;
    this.employeeDetailedOpened = true;
  }

  private reset(): void {
    this.img = null;
    this.error = "";
    this.loading = false;
    this.employeeDetailedOpened = false;
    this.selectedEmployee = null;
    this.employees = [];
  }
}

export default MapPreviewData;
