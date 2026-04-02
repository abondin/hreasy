<!--
  Shared titled section card for detail and side-panel layouts.
-->
<template>
  <v-card>
    <v-card-item>
      <template #title>
        <span :class="titleClass">{{ title }}</span>
      </template>
    </v-card-item>
    <v-card-text class="pt-0 px-6 pb-6">
      <slot />
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
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
