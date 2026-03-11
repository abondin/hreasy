import { computed, ref, type Ref } from "vue";
import type { Employee } from "@/services/employee.service";
import { usePermissions } from "@/lib/permissions";

export function useEmployeeProjectActions(
  employee: Ref<Employee>,
  readOnly?: Ref<boolean>,
) {
  const projectInfoDialogOpen = ref(false);
  const projectUpdateDialogOpen = ref(false);
  const permissions = usePermissions();

  const canShowProjectInfo = computed(
    () => Boolean(employee.value.currentProject?.id),
  );

  const canEditProject = computed(
    () =>
      !Boolean(readOnly?.value) &&
      Boolean(employee.value.id && permissions.canUpdateCurrentProject(employee.value.id)),
  );

  function openProjectInfo() {
    if (canShowProjectInfo.value) {
      projectInfoDialogOpen.value = true;
    }
  }

  function openProjectUpdate() {
    if (canEditProject.value) {
      projectUpdateDialogOpen.value = true;
    }
  }

  return {
    canShowProjectInfo,
    canEditProject,
    projectInfoDialogOpen,
    projectUpdateDialogOpen,
    openProjectInfo,
    openProjectUpdate,
  };
}
