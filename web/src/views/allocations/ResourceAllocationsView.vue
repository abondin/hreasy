<template>
  <TableFirstPageLayout test-id="resource-allocations-view">
    <v-tabs
      :model-value="activeTab"
      color="primary"
      class="resource-allocation-tabs flex-shrink-0"
      data-testid="resource-allocations-tabs"
    >
      <v-tab
        value="input"
        :to="{ name: 'resource-allocations-input' }"
        data-testid="resource-allocations-tab-input"
      >
        {{ t("Ввод данных") }}
      </v-tab>
      <v-tab
        value="analytics"
        :to="{ name: 'resource-allocations-analytics' }"
        data-testid="resource-allocations-tab-analytics"
      >
        {{ t("Аналитика") }}
      </v-tab>
    </v-tabs>
    <router-view class="flex-grow-1 min-h-0" />
  </TableFirstPageLayout>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import TableFirstPageLayout from "@/components/shared/TableFirstPageLayout.vue";

defineOptions({ name: "ResourceAllocationsView" });

const { t } = useI18n();
const route = useRoute();
const activeTab = computed(() =>
  route.name === "resource-allocations-analytics" ? "analytics" : "input",
);
</script>

<style scoped>
.resource-allocation-tabs {
  position: relative;
  z-index: 2;
  background: rgb(var(--v-theme-surface));
}
</style>
