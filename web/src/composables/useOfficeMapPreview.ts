import { computed, ref, type Ref } from "vue";
import type { Employee } from "@/services/employee.service";

export function useOfficeMapPreview(employee: Ref<Employee>) {
  const mapDialogOpen = ref(false);
  const mapName = computed(() => employee.value.officeLocation?.mapName ?? null);
  const mapTitle = computed(() => employee.value.officeLocation?.name ?? null);
  const highlightedWorkplace = computed(
    () => employee.value.officeWorkplace ?? null,
  );

  function openMap() {
    if (mapName.value) {
      mapDialogOpen.value = true;
    }
  }

  return {
    mapDialogOpen,
    mapName,
    mapTitle,
    highlightedWorkplace,
    openMap,
  };
}
