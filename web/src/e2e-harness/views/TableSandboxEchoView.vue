<template>
  <v-container class="py-6" data-testid="table-sandbox-echo-view">
    <div class="d-flex flex-column ga-4">
      <div>
        <v-btn
          color="primary"
          variant="outlined"
          data-testid="table-sandbox-echo-back"
          :to="backTarget"
        >
          Back To Sandbox
        </v-btn>
      </div>

      <v-card>
        <v-card-title>Echo Route</v-card-title>
        <v-card-text>
          This page is intentionally taller than the viewport to force browser scrolling during keep-alive tests.
        </v-card-text>
      </v-card>

      <div style="min-height: 1600px" />
    </div>
  </v-container>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRoute } from "vue-router";

defineOptions({
  name: "TableSandboxEchoView",
});

const route = useRoute();

const backTarget = computed(() => {
  const returnTo = route.query.returnTo;
  return typeof returnTo === "string" && returnTo.startsWith("/")
    ? returnTo
    : { name: "table-sandbox" };
});
</script>
