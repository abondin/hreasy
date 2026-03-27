<template>
  <v-text-field
    :model-value="props.label"
    :data-testid="props.labelTestId"
    readonly
    :variant="props.variant"
    density="compact"
    hide-details
    class="text-center"
    style="width: 19rem; min-width: 19rem; max-width: 19rem;"
    :disabled="props.disabled"
  >
    <template #prepend-inner>
      <v-btn
        icon="mdi-chevron-left"
        variant="text"
        size="small"
        density="comfortable"
        :disabled="props.disabled"
        :data-testid="props.prevTestId"
        @click="$emit('prev')"
      />

      <v-tooltip v-if="props.statusIcon && props.statusIconTitle" location="bottom">
        <template #activator="{ props: tooltipProps }">
          <v-icon
            v-bind="tooltipProps"
            :icon="props.statusIcon"
            :color="props.statusIconColor"
            size="small"
          />
        </template>
        <span>{{ props.statusIconTitle }}</span>
      </v-tooltip>
      <v-icon
        v-else-if="props.statusIcon"
        :icon="props.statusIcon"
        :color="props.statusIconColor"
        size="small"
      />
    </template>

    <template #append-inner>
      <v-tooltip v-if="!props.isCurrent" location="bottom">
        <template #activator="{ props: tooltipProps }">
          <v-btn
            v-bind="tooltipProps"
            icon="mdi-calendar-refresh"
            size="x-small"
            color="primary"
            variant="text"
            :disabled="props.disabled"
            @click="$emit('go-current')"
          />
        </template>
        <span>{{ props.goCurrentLabel }}</span>
      </v-tooltip>

      <v-btn
        icon="mdi-chevron-right"
        variant="text"
        size="small"
        density="comfortable"
        :disabled="props.disabled"
        :data-testid="props.nextTestId"
        @click="$emit('next')"
      />
    </template>
  </v-text-field>
</template>

<script setup lang="ts">
interface Props {
  label: string;
  isCurrent?: boolean;
  goCurrentLabel?: string;
  disabled?: boolean;
  prevTestId?: string;
  nextTestId?: string;
  labelTestId?: string;
  statusIcon?: string;
  statusIconColor?: string;
  statusIconTitle?: string;
  variant?: "outlined" | "solo";
}

const props = withDefaults(defineProps<Props>(), {
  isCurrent: true,
  goCurrentLabel: "Перейти к текущему",
  disabled: false,
  prevTestId: undefined,
  nextTestId: undefined,
  labelTestId: undefined,
  statusIcon: undefined,
  statusIconColor: undefined,
  statusIconTitle: undefined,
  variant: "outlined",
});

defineEmits<{
  prev: [];
  next: [];
  "go-current": [];
}>();
</script>
