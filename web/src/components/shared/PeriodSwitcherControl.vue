<template>
  <div class="period-switcher-control">
    <v-btn
      icon="mdi-chevron-left"
      variant="text"
      :disabled="props.disabled"
      :data-testid="props.prevTestId"
      @click="$emit('prev')"
    />
    <div class="period-switcher-center">
      <span class="period-switcher-label" :data-testid="props.labelTestId">
        {{ props.label }}
      </span>
      <div class="period-switcher-action-slot">
        <v-tooltip v-if="!props.isCurrent" location="bottom">
          <template #activator="{ props: tooltipProps }">
            <v-btn
              v-bind="tooltipProps"
              icon="mdi-calendar-refresh"
              size="x-small"
              color="primary"
              variant="text"
              :disabled="props.disabled"
              class="period-switcher-go-current-icon"
              @click="$emit('go-current')"
            />
          </template>
          <span>{{ props.goCurrentLabel }}</span>
        </v-tooltip>
      </div>
    </div>
    <v-btn
      icon="mdi-chevron-right"
      variant="text"
      :disabled="props.disabled"
      :data-testid="props.nextTestId"
      @click="$emit('next')"
    />
  </div>
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
}

const props = withDefaults(defineProps<Props>(), {
  isCurrent: true,
  goCurrentLabel: "Перейти к текущему",
  disabled: false,
  prevTestId: undefined,
  nextTestId: undefined,
  labelTestId: undefined,
});

defineEmits<{
  prev: [];
  next: [];
  "go-current": [];
}>();
</script>

<style scoped>
.period-switcher-control {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.period-switcher-center {
  width: calc(11rem + 18px);
  display: flex;
  justify-content: center;
  align-items: center;
}

.period-switcher-label {
  width: 11rem;
  text-align: center;
}

.period-switcher-action-slot {
  width: 18px;
  min-width: 18px;
  display: flex;
  justify-content: center;
  margin-left: -1px;
}

.period-switcher-go-current-icon {
  margin: 0;
}
</style>
