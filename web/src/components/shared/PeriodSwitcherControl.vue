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

      <v-sheet width="28" color="transparent" class="d-inline-flex align-center justify-center">
        <v-tooltip v-if="props.periodClosed" location="bottom">
          <template #activator="{ props: tooltipProps }">
            <v-btn
              v-bind="tooltipProps"
              icon="mdi-lock"
              disabled
              variant="plain"
              size="x-small"
              density="comfortable"
            />
          </template>
          <span>{{ t("Период закрыт для внесения изменений") }}</span>
        </v-tooltip>
      </v-sheet>
    </template>

    <template #append-inner>
      <v-sheet width="28" color="transparent" class="d-inline-flex align-center justify-center">
        <v-tooltip v-if="!props.isCurrent" location="bottom">
          <template #activator="{ props: tooltipProps }">
            <v-btn
              v-bind="tooltipProps"
              icon="mdi-calendar-cursor-outline"
              variant="plain"
              size="x-small"
              density="comfortable"
              :disabled="props.disabled"
              @click="$emit('go-current')"
            />
          </template>
          <span>{{ t("Перейти к текущему") }}</span>
        </v-tooltip>
      </v-sheet>

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
import { useI18n } from "vue-i18n";

interface Props {
  label: string;
  isCurrent?: boolean;
  disabled?: boolean;
  nextTestId?: string;
  labelTestId?: string;
  periodClosed?: boolean;
  prevTestId?: string;
  variant?: "underlined" | "outlined" | "filled" | "solo" | "solo-inverted" | "solo-filled" | "plain";
  width?: string | number;
}

const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  isCurrent: true,
  disabled: false,
  periodClosed: false,
  variant: "outlined",
  width: 248,
});

defineEmits<{
  prev: [];
  next: [];
  "go-current": [];
}>();
</script>

