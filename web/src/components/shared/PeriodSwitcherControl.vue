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
      <button
        v-if="!props.isCurrent"
        type="button"
        class="period-switcher-go-current"
        :disabled="props.disabled"
        @click="$emit('go-current')"
      >
        {{ props.goCurrentLabel }}
      </button>
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
  align-items: flex-start;
  gap: 8px;
}

.period-switcher-center {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.period-switcher-label {
  display: inline-block;
  min-width: 11rem;
  max-width: 11rem;
  text-align: center;
}

.period-switcher-go-current {
  margin-top: 2px;
  border: 0;
  background: transparent;
  padding: 0;
  font-size: 11px;
  line-height: 1;
  color: rgb(var(--v-theme-primary));
  text-decoration: underline;
  cursor: pointer;
}

.period-switcher-go-current:disabled {
  opacity: 0.55;
  cursor: default;
  text-decoration: none;
}
</style>
