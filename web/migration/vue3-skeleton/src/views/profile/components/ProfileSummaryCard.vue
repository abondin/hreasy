<!--
  Employee base information card.
  In editable mode project/role, avatar and telegram can be changed if permissions allow.
  -->
<template>
  <v-card class="profile-summary-card pa-6">
    <v-row align="center" no-gutters>
      <v-col cols="auto" class="mr-6">
        <profile-avatar
          :owner="employee"
          :read-only="false"
          @updated="onAvatarUpdated"
        />
      </v-col>

      <v-col>
        <div class="profile-summary-card__name">
          {{ employee.displayName }}
        </div>
        <div
          v-for="item in items"
          :key="item.key"
          class="profile-summary-card__item"
        >
          <span class="profile-summary-card__label">{{ item.label }}:</span>
          <span class="profile-summary-card__value">{{ item.value }}</span>
        </div>
      </v-col>
    </v-row>
  </v-card>
</template>

<script setup lang="ts">
import ProfileAvatar from "@/views/profile/components/ProfileAvatar.vue";
import type { WithAvatar } from "@/services/employee.service";

interface SummaryItem {
  key: string;
  label: string;
  value: string;
}

defineProps<{
  employee: WithAvatar;
  items: SummaryItem[];
}>();

const emit = defineEmits<{
  (event: "avatar-updated"): void;
}>();

function onAvatarUpdated() {
  emit("avatar-updated");
}
</script>

<style scoped>
.profile-summary-card__name {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 12px;
}

.profile-summary-card__item {
  font-size: 0.95rem;
  color: rgba(0, 0, 0, 0.72);
  margin-bottom: 6px;
}

.profile-summary-card__label {
  font-weight: 500;
  margin-right: 4px;
}

.profile-summary-card__value {
  font-weight: 400;
}
</style>
