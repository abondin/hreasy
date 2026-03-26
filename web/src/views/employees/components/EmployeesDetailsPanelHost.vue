<!--
  Employees details panel host.
  Chooses drawer vs fullscreen dialog based on responsive mode.
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
    disable-route-watcher
    :width="drawerWidth"
  >
    <v-toolbar density="comfortable" border="b">
      <v-spacer />
      <v-btn icon="mdi-close" variant="text" @click="openModel = false" />
    </v-toolbar>

    <div class="pa-2">
      <employee-details-panel
        v-if="employee"
        :employee="employee"
        @employee-updated="emit('employee-updated')"
      />
    </div>
  </v-navigation-drawer>
</template>

<script setup lang="ts">
import { computed } from "vue";
import EmployeeDetailsPanel from "@/views/employees/components/EmployeeDetailsPanel.vue";
import type { Employee } from "@/services/employee.service";

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
</script>
