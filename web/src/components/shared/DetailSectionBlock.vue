<!--
  Shared titled section block without its own card surface.
  Useful inside larger summary cards.
-->
<template>
  <section>
    <div :class="resolvedTitleClass">
      {{ title }}
    </div>
    <slot />
  </section>
</template>

<script setup lang="ts">
import { computed } from "vue";

const props = withDefaults(
  defineProps<{
    title: string;
    titleClass?: string;
  }>(),
  {
    titleClass: "text-body-2 font-weight-medium mb-2",
  },
);

const resolvedTitleClass = computed(() => props.titleClass);
</script>