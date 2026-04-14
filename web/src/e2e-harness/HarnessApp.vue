<template>
  <v-app>
    <v-app-bar elevation="1" density="comfortable">
      <v-app-bar-title data-testid="table-harness-title">
        HR Easy Table Harness
      </v-app-bar-title>
    </v-app-bar>

    <v-main>
      <RouterView v-slot="{ Component }">
        <KeepAlive :include="keepAliveComponentNames">
          <component :is="Component" />
        </KeepAlive>
      </RouterView>
    </v-main>
  </v-app>
</template>

<script setup lang="ts">
import { RouterView } from "vue-router";

const keepAliveComponentNames = [
  "TableSandboxView",
  "TableSandboxPlainView",
  "TableSandboxVacationsLikeView",
  "TableSandboxOvertimesLikeView",
];
</script>
