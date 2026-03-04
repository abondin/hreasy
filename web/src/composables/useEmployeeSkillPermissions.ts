import { computed } from "vue";
import { usePermissions } from "@/lib/permissions";

export function useEmployeeSkillPermissions(employeeId: () => number | null) {
  const permissions = usePermissions();

  const canViewSkills = computed(() => {
    const id = employeeId();
    if (!id) {
      return false;
    }
    return permissions.canViewEmplSkills(id);
  });

  const canEditSkills = computed(() => {
    const id = employeeId();
    if (!id) {
      return false;
    }
    return permissions.canEditSkills(id);
  });

  const canAddSkills = computed(() => {
    const id = employeeId();
    if (!id) {
      return false;
    }
    return permissions.canAddSkills(id);
  });

  const canDeleteSkills = computed(() => {
    const id = employeeId();
    if (!id) {
      return false;
    }
    return permissions.canDeleteSkills(id);
  });

  const canRateSkills = computed(() => {
    const id = employeeId();
    if (!id) {
      return false;
    }
    return permissions.canRateSkills(id);
  });

  return {
    canViewSkills,
    canEditSkills,
    canAddSkills,
    canDeleteSkills,
    canRateSkills,
  };
}
