<!--
  Employee base information card wrapper.
-->
<template>
  <v-card class="pa-6">
    <profile-summary
      :employee="employee"
      :read-only="readOnly"
      :show-name="showName"
      @avatar-updated="$emit('avatar-updated')"
      @edit-telegram="$emit('edit-telegram')"
      @update-project="$emit('update-project')"
    />
  </v-card>
</template>

<script setup lang="ts">
import ProfileSummary from "@/views/profile/components/ProfileSummary.vue";
import type { Employee } from "@/services/employee.service";

withDefaults(
  defineProps<{
    employee: Employee;
    readOnly?: boolean;
    showName?: boolean;
  }>(),
  {
    readOnly: true,
    showName: true,
  },
);

defineEmits<{
  (event: "avatar-updated"): void;
  (event: "edit-telegram"): void;
  (event: "update-project"): void;
}>();
</script>
