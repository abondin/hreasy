import http from "@/lib/http";

export interface SimpleDict {
  id: number;
  name: string;
  active?: boolean;
}

export interface ManagerOfObject {
  id: number;
  employeeId: number;
  employeeName: string;
  responsibilityType: string;
  comment?: string;
}

export interface ProjectDictDto extends SimpleDict {
  baId: number;
}

export interface CurrentProjectRole {
  value: string;
}

export interface ProjectInfo {
  id: number;
  name: string;
  startDate?: string;
  planStartDate?: string;
  planEndDate?: string;
  endDate?: string;
  customer?: string;
  department: SimpleDict;
  businessAccount: SimpleDict;
  info?: string;
  managers: ManagerOfObject[];
  baManagers: ManagerOfObject[];
}

export async function fetchProjects(): Promise<ProjectDictDto[]> {
  const response = await http.get<ProjectDictDto[]>("v1/dict/projects");
  return response.data;
}

export async function fetchCurrentProjectRoles(): Promise<
  CurrentProjectRole[]
> {
  const response = await http.get<CurrentProjectRole[]>(
    "v1/employee/current_project_roles",
  );
  return response.data;
}

export async function fetchProjectInfo(
  projectId: number,
): Promise<ProjectInfo> {
  const response = await http.get<ProjectInfo>(`v1/projects/${projectId}`);
  return response.data;
}
