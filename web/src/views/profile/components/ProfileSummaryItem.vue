<template>
  <property-list-item :label="label">
    <slot />
  </property-list-item>
</template>

<script setup lang="ts">
import PropertyListItem from "@/components/shared/PropertyListItem.vue";

defineProps<{
  label: string;
}>();
</script>
