<!--
  Employee base information card.
  In editable mode project/role, avatar and telegram can be changed if permissions allow.
  -->
<template>
  <v-card class="pa-6">
    <v-row align="center">
      <!--<editor-fold desc="Avatar">-->
      <v-col cols="auto">
        <v-avatar size="164" class="profile-avatar">
          <v-img
              v-if="avatarUrl"
              :src="avatarUrl"
              :alt="displayName"/>
          <v-icon v-else icon="mdi-account-circle" size="120" color="grey"/>
        </v-avatar>
      </v-col>
      <!-- </editor-fold> -->
      <!--<editor-fold desc="Employee basic info">-->
      <v-col>
        <v-list-item>
          <v-list-item-content>
            <v-list-item-title class="title">
              {{ displayName }}
            </v-list-item-title>
            <v-list-item-subtitle v-for="item in items" :key="item.key">
              {{ item.label }}: {{ item.value }}
            </v-list-item-subtitle>
          </v-list-item-content>
        </v-list-item>
      </v-col>
      <!-- </editor-fold> -->
    </v-row>
  </v-card>
</template>

<script setup lang="ts">
// Renders the employee summary header on the profile page;
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

</style>
