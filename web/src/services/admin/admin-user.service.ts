import http from "@/lib/http";
import type { DictItem } from "@/services/dict.service";

export interface UserSecurityInfo {
  employee: DictItem;
  userId?: number;
  departmentId?: number;
  currentProjectId?: number;
  roles: string[];
  accessibleDepartments: number[];
  accessibleProjects: number[];
  accessibleBas: number[];
  dateOfDismissal: string;
}

export interface UserRolesUpdateBody {
  roles: string[];
  accessibleDepartments: number[];
  accessibleProjects: number[];
  accessibleBas: number[];
}

export interface RoleDict {
  id: string;
  name: string;
  disabled: boolean;
}

/** Admin CRUD contract for user roles and access scopes. */
export async function listAdminUsers(): Promise<UserSecurityInfo[]> {
  const response = await http.get<UserSecurityInfo[]>("v1/admin/users");
  return response.data;
}

export async function updateAdminUserRoles(
  employeeId: number,
  body: UserRolesUpdateBody,
): Promise<number> {
  const response = await http.put<number>(`v1/admin/users/roles/${employeeId}`, body);
  return response.data;
}

/**
 * Static role dictionary mirrored from legacy until a backend dictionary exists.
 */
export function getAdminUserRolesDictionary(
  t: (key: string) => string,
): RoleDict[] {
  return [
    { id: "global_admin", name: t("role.global_admin"), disabled: true },
    { id: "hr", name: t("role.hr"), disabled: false },
    { id: "pm", name: t("role.pm"), disabled: false },
    { id: "pm_finance", name: t("role.pm_finance"), disabled: false },
    { id: "salary_manager", name: t("role.salary_manager"), disabled: false },
    { id: "content_management", name: t("role.content_management"), disabled: false },
    { id: "mentor", name: t("role.mentor"), disabled: false },
  ];
}
