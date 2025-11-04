<!--
  Markdown editor with preview toggle.
  Ported from the legacy implementation.
  -->
<template>
  <div class="markdown-editor">
    <v-card outlined>
      <v-card-title class="markdown-editor__title">
        <span>{{ label }}</span>
        <v-spacer />
        <div class="preview-toggle" @click.stop>
          <v-switch
            v-model="preview"
            :label="t('Предпросмотр')"
            inset
            density="compact"
            hide-details
          />
        </div>
      </v-card-title>
      <v-divider />
      <v-card-text :style="contentStyle">
        <template v-if="!preview">
          <v-textarea
            :model-value="localValue"
            rows="6"
            :aria-label="label"
            :placeholder="placeholder"
            :hint="hint"
            :rules="rules"
            :counter="textareaCounter"
            persistent-hint
            @update:model-value="onInput"
          />
        </template>
        <template v-else>
          <markdown-text-renderer :content="localValue" />
        </template>
      </v-card-text>
      <div v-if="reservePreviewDetails" class="details-reserve"></div>
    </v-card>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import MarkdownTextRenderer from "@/components/shared/MarkdownTextRenderer.vue";

const props = withDefaults(
  defineProps<{
    modelValue?: string;
    label?: string;
    placeholder?: string;
    hint?: string;
    rules?: Array<(value: unknown) => boolean | string>;
    counter?: number | boolean;
    minHeight?: number;
    reserveDetailsSpace?: boolean;
  }>(),
  {
    modelValue: "",
    label: "Комментарий в Markdown",
    placeholder: "Введите текст: **жирный**, `код`, [ссылка](https://...)",
    hint: "Поддерживается Markdown: заголовки, списки, ссылки, код.",
    rules: () => [],
    counter: undefined,
    minHeight: 240,
    reserveDetailsSpace: false,
  },
);

const emit = defineEmits<{
  (event: "update:modelValue", value: string): void;
  (event: "change", value: string): void;
}>();

const { t } = useI18n();

const preview = ref(false);
const localValue = ref(props.modelValue ?? "");

const contentStyle = computed(() => ({
  minHeight: `${props.minHeight}px`,
}));

const textareaCounter = computed(() =>
  props.counter === false ? undefined : props.counter,
);

const reservePreviewDetails = computed(() => {
  if (!props.reserveDetailsSpace) {
    return false;
  }
  const hasRules = Array.isArray(props.rules) && props.rules.length > 0;
  const hasHint = Boolean(props.hint);
  const hasCounter = props.counter !== undefined && props.counter !== false;
  return hasRules || hasHint || hasCounter;
});

watch(
  () => props.modelValue,
  (value) => {
    if (value !== undefined) {
      localValue.value = value;
    }
  },
);

function onInput(value: string) {
  localValue.value = value;
  emit("update:modelValue", value);
  emit("change", value);
}
</script>

<style scoped>
.markdown-editor__title {
  font-size: 0.9rem;
  color: rgba(0, 0, 0, 0.72);
  display: flex;
  align-items: center;
}

.preview-toggle {
  display: flex;
  align-items: center;
}

.preview-toggle .v-input--switch {
  margin-top: 0;
}

.details-reserve {
  min-height: 22px;
}
</style>
