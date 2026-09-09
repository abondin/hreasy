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
    <router-view class="flex-grow-1 min-h-0" @open-employee="selectedEmployeeId = $event" />
    <ResourceAllocationEmployeeDialog
      v-if="selectedEmployeeId != null"
      :key="selectedEmployeeId"
      :employee-id="selectedEmployeeId"
      @close="selectedEmployeeId = null"
    />
  </TableFirstPageLayout>
</template>

<script setup lang="ts">
import { computed, onDeactivated, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import TableFirstPageLayout from "@/components/shared/TableFirstPageLayout.vue";
import ResourceAllocationEmployeeDialog from "@/views/allocations/ResourceAllocationEmployeeDialog.vue";

defineOptions({ name: "ResourceAllocationsView" });

const { t } = useI18n();
const route = useRoute();
const selectedEmployeeId = ref<number | null>(null);
watch(() => route.name, () => { selectedEmployeeId.value = null; });
onDeactivated(() => { selectedEmployeeId.value = null; });
const activeTab = computed(() =>
  route.name === "resource-allocations-analytics" ? "analytics" : "input",
);
</script>

<style scoped>
@media (hover: hover) and (pointer: fine) {
  :deep(.resource-allocation-employee-details-button) {
    opacity: 0;
  }

  :deep(.rgRow:hover .resource-allocation-employee-details-button),
  :deep(.resource-allocation-employee-details-button:focus-visible) {
    opacity: 1;
  }
}

.resource-allocation-tabs {
  position: relative;
  z-index: 2;
  background: rgb(var(--v-theme-surface));
}
</style>
