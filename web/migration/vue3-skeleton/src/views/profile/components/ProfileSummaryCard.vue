<template>
  <v-card class="profile-card pa-6">
    <v-row align="center">
      <v-col
          cols="auto"
          class="d-flex justify-center justify-md-start mb-4 mb-md-0" >
        <v-avatar
            size="144"
            class="profile-avatar">
          <v-img
              v-if="avatarUrl"
              :src="avatarUrl"
              :alt="displayName"
          />
          <v-icon
              v-else
              icon="mdi-account-circle"
              size="120"
              color="grey"
          />
        </v-avatar>
      </v-col>
      <v-col
          cols="12"
          md="8"
          class="profile-info"
      >
        <v-list density="compact" lines="one">
          <v-list-item>
            <v-list-item-title class="text-h6">
              {{ displayName }}
            </v-list-item-title>
          </v-list-item>
          <template v-for="item in items" :key="item.key">
            <v-list-item density="compact">
              <v-list-item-title>
                {{ item.label }}: {{ item.value }}
              </v-list-item-title>
            </v-list-item>
          </template>
        </v-list>
      </v-col>
    </v-row>
  </v-card>
</template>

<script setup lang="ts">
// Renders the employee summary header on the profile page; used by ProfileMainView during Vue 3 migration.
interface SummaryItem {
  key: string;
  label: string;
  value: string;
}

defineProps<{
  displayName: string;
  avatarUrl?: string;
  items: SummaryItem[];
}>();
</script>

<style scoped>
.profile-card {
  border-radius: 18px;
}

.profile-avatar {
  background-color: rgba(0, 0, 0, 0.04);
}

.profile-info {
  padding-left: 12px;
}

.profile-info :deep(.v-list) {
  padding: 0;
}

.profile-info :deep(.v-list-item) {
  min-height: 32px;
  padding-top: 0;
  padding-bottom: 0;
}

.profile-info :deep(.v-list-item-title) {
  line-height: 1.3;
}

@media (min-width: 960px) {
  .profile-info {
    padding-left: 20px;
  }
}
</style>
