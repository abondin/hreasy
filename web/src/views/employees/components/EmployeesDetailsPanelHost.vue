<!--
  Employees details panel host.
  Uses fullscreen dialog on mobile and right-side sheet on desktop.
-->
<template>
  <v-dialog
    v-if="useFullscreen"
    v-model="openModel"
    fullscreen
    scrollable
  >
    <v-card>
      <v-toolbar density="comfortable" border="b">
        <v-spacer />
        <v-btn icon="mdi-close" variant="text" @click="openModel = false" />
      </v-toolbar>

      <v-card-text class="pa-2">
        <employee-details-panel
          v-if="employee"
          :employee="employee"
          @employee-updated="emit('employee-updated')"
        />
      </v-card-text>
    </v-card>
  </v-dialog>

  <v-navigation-drawer
    v-else
    v-model="openModel"
    data-testid="employees-details-drawer"
    location="right"
    temporary
    floating
    disable-route-watcher
    :scrim="false"
    :width="drawerWidth"
  >
    <div
      v-click-outside="onDesktopOutsideClick"
      class="h-100 d-flex flex-column"
    >
      <v-toolbar density="comfortable" border="b">
        <v-spacer />
        <v-btn icon="mdi-close" variant="text" @click="openModel = false" />
      </v-toolbar>

      <div class="pa-2 flex-1-1 overflow-y-auto">
        <employee-details-panel
          v-if="employee"
          :employee="employee"
          @employee-updated="emit('employee-updated')"
        />
      </div>
    </div>
  </v-navigation-drawer>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted } from "vue";
import type { Employee } from "@/services/employee.service";
import EmployeeDetailsPanel from "@/views/employees/components/EmployeeDetailsPanel.vue";

const props = defineProps<{
  modelValue: boolean;
  employee: Employee | null;
  useFullscreen: boolean;
  drawerWidth: number;
}>();

const emit = defineEmits<{
  (event: "update:modelValue", value: boolean): void;
  (event: "employee-updated"): void;
}>();

const openModel = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit("update:modelValue", value),
});

function onDesktopOutsideClick(event: MouseEvent) {
  const target = event.target;
  if (!(target instanceof Element)) {
    openModel.value = false;
    return;
  }

  if (target.closest(".v-overlay")) {
    return;
  }

  if (target.closest('[data-testid="employees-table"] tbody tr')) {
    return;
  }

  openModel.value = false;
}

function onWindowKeydown(event: KeyboardEvent) {
  if (props.useFullscreen || !openModel.value) {
    return;
  }
  if (event.key === "Escape") {
    openModel.value = false;
  }
}

onMounted(() => {
  window.addEventListener("keydown", onWindowKeydown);
});

onBeforeUnmount(() => {
  window.removeEventListener("keydown", onWindowKeydown);
});
</script>
