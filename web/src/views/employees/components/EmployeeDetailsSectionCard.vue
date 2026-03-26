<!--
  Compatibility wrapper around the shared detail section card.
-->
<template>
  <detail-section-card :title="title">
    <slot />
  </detail-section-card>
</template>

<script setup lang="ts">
import DetailSectionCard from "@/components/shared/DetailSectionCard.vue";

defineProps<{
  title: string;
}>();
</script>