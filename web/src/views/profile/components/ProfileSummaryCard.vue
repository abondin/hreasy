<!--
  Employee base information card wrapper.
-->
<template>
  <v-card class="pa-6">
    <v-card-item v-if="headerTitle" class="px-0 pt-0">
      <template #title>{{ headerTitle }}</template>
    </v-card-item>
    <v-row align="start" class="ga-4">
      <v-col cols="12" lg="auto">
        <profile-summary
          :employee="employee"
          :read-only="readOnly"
          :avatar-read-only="avatarReadOnly"
          :project-read-only="projectReadOnly"
          :show-name="showName"
          @avatar-updated="$emit('avatar-updated')"
          @edit-telegram="$emit('edit-telegram')"
          @update-project="$emit('update-project')"
        />
      </v-col>
      <v-col v-if="$slots.default" cols="12" lg class="min-w-0">
        <slot />
      </v-col>
    </v-row>
  </v-card>
</template>

<script setup lang="ts">
import { computed } from "vue";
import ProfileSummary from "@/views/profile/components/ProfileSummary.vue";
import type { Employee } from "@/services/employee.service";

const props = withDefaults(
  defineProps<{
    employee: Employee;
    readOnly?: boolean;
    avatarReadOnly?: boolean;
    projectReadOnly?: boolean;
    showName?: boolean;
    title?: string | null;
  }>(),
  {
    readOnly: true,
    avatarReadOnly: undefined,
    projectReadOnly: undefined,
    showName: true,
    title: null,
  },
);

const headerTitle = computed(() => props.title ?? (!props.showName ? props.employee.displayName : null));

defineEmits<{
  (event: "avatar-updated"): void;
  (event: "edit-telegram"): void;
  (event: "update-project"): void;
}>();
</script>
