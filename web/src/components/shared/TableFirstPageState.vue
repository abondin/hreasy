<template>
  <v-sheet class="d-flex align-center justify-center h-100" rounded border>
    <v-empty-state
      :data-testid="props.testId"
      :icon="props.icon"
      :title="props.title"
      :text="props.text"
      :color="props.color"
      justify="center"
    >
      <template v-if="props.actionText" #actions>
        <v-btn
          :color="props.color ?? 'surface-variant'"
          :to="props.to"
          :text="props.actionText"
        />
      </template>
    </v-empty-state>
  </v-sheet>
</template>

<script setup lang="ts">
const props = withDefaults(defineProps<{
  testId?: string;
  title: string;
  text?: string;
  icon?: string;
  color?: string;
  actionText?: string;
  to?: string | Record<string, unknown>;
}>(), {
  testId: undefined,
  text: undefined,
  icon: "mdi-information-outline",
  color: undefined,
  actionText: undefined,
  to: undefined,
});
</script>
