<!--
  Secondary section card inside employee details panel.
  Standardizes spacing and section title rendering.
-->
<template>
  <v-card>
    <v-card-text class="pa-6">
      <div class="text-body-2 font-weight-medium mb-2">
        {{ title }}
      </div>
      <slot />
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
defineProps<{
  title: string;
}>();
</script>
