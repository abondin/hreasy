<!--
  Compact summary section for admin detail pages without its own surface.
-->
<template>
  <div class="admin-detail-summary-card">
    <div v-if="title" class="text-subtitle-2 mb-3">
      {{ title }}
    </div>
    <div>
      <property-list variant="aligned" density="compact">
        <template
          v-for="item in items"
          :key="item.label"
        >
          <profile-summary-item :label="item.label">
            <span :class="item.valueClass">{{ item.value }}</span>
          </profile-summary-item>
        </template>
      </property-list>
    </div>
  </div>
</template>

<script setup lang="ts">
import PropertyList from "@/components/shared/PropertyList.vue";
import ProfileSummaryItem from "@/views/profile/components/ProfileSummaryItem.vue";

export interface AdminDetailSummaryItem {
  label: string;
  value: string;
  valueClass?: string;
}

defineProps<{
  items: AdminDetailSummaryItem[];
  title?: string;
}>();
</script>

<style scoped>
.admin-detail-summary-card {
  min-width: 320px;
}
</style>
