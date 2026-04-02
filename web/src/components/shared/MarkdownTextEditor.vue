<!--
  Markdown editor with preview toggle.
  -->
<template>
  <div>
    <v-card density="compact">
      <v-card-item>
        <template #title>
          <span class="text-body-large">{{ label }}</span>
        </template>
        <template #append>
          <div class="d-flex align-center" @click.stop>
            <v-switch
              v-model="preview"
              :label="t('Предпросмотр')"
              class="my-0"
              inset
              density="compact"
              hide-details
            />
          </div>
        </template>
      </v-card-item>
      <v-divider />
      <v-card-text class="pa-0">
        <v-sheet color="transparent" :min-height="minHeight">
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
        </v-sheet>
      </v-card-text>
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
  }>(),
  {
    modelValue: "",
    label: "Комментарий в Markdown",
    placeholder: "Введите текст: **жирный**, `код`, [ссылка](https://...)",
    hint: "Поддерживается Markdown: заголовки, списки, ссылки, код.",
    rules: () => [],
    counter: undefined,
    minHeight: 240,
  },
);

const emit = defineEmits<{
  (event: "update:modelValue", value: string): void;
  (event: "change", value: string): void;
}>();

const { t } = useI18n();

const preview = ref(false);
const localValue = ref(props.modelValue ?? "");
const minHeight = computed(() => props.minHeight);

const textareaCounter = computed(() =>
  props.counter === false ? undefined : props.counter,
);

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
