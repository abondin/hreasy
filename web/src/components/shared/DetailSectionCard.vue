<!--
  Shared titled section card for detail and side-panel layouts.
-->
<template>
  <v-card>
    <v-card-text class="pa-6">
      <detail-section-block :title="title" :title-class="titleClass">
        <slot />
      </detail-section-block>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import DetailSectionBlock from "@/components/shared/DetailSectionBlock.vue";

withDefaults(
  defineProps<{
    title: string;
    titleClass?: string;
  }>(),
  {
    titleClass: "text-body-2 font-weight-medium mb-2",
  },
);
</script>