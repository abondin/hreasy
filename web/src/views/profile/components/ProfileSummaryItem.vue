<template>
  <property-list-item :label="label" :truncate-label="truncateLabel" :truncate-value="truncateValue">
    <slot />
  </property-list-item>
</template>

<script setup lang="ts">
import PropertyListItem from "@/components/shared/PropertyListItem.vue";

defineProps<{
  label: string;
  truncateLabel?: boolean;
  truncateValue?: boolean;
}>();
</script>
