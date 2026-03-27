<template>
  <div class="d-flex flex-column flex-grow-1 h-100 min-h-0 overflow-auto">
    <VacationsTimeline
      :vacations="items"
      :year="year"
      @year-navigation="emit('yearNavigation', $event)"
    />
  </div>
</template>

<script setup lang="ts">
import VacationsTimeline from "@/components/vacations/VacationsTimeline.vue";
import type { Vacation } from "@/services/vacation.service";

defineProps<{
  items: Vacation[];
  year: number;
}>();

const emit = defineEmits<{
  (event: "yearNavigation", year: number): void;
}>();
</script>
