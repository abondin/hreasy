<template>
  <v-text-field
    :model-value="props.label"
    :data-testid="props.labelTestId"
    readonly
    :variant="props.variant"
    density="compact"
    hide-details
    :width="props.width"
    :min-width="props.width"
    :max-width="props.width"
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
          <v-btn
            v-bind="tooltipProps"
            :icon="props.statusIcon"
            :color="props.statusIconColor"
            disabled
            variant="text"
            size="small"
            density="comfortable"
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
            color="primary"
            variant="text"
            size="small"
            density="comfortable"
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
  variant?: "underlined" | "outlined" | "filled" | "solo" | "solo-inverted" | "solo-filled" | "plain";
  width?: string | number;
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
  width: 248,
});

defineEmits<{
  prev: [];
  next: [];
  "go-current": [];
}>();
</script>
