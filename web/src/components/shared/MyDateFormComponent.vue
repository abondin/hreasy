<!--
  Date input with calendar picker and DD.MM.YYYY text support.
-->
<template>
  <v-menu
    v-model="menuOpen"
    :close-on-content-click="false"
    transition="scale-transition"
    offset-y
    min-width="auto"
    max-width="320"
  >
    <template #activator="{ props: activatorProps }">
      <v-text-field
        v-model="formattedValue"
        clearable
        :label="label"
        :rules="fieldRules"
        @update:model-value="handleTextUpdated"
        @click:clear="clearValue"
      >
        <template #append-inner>
          <v-btn
            icon="mdi-calendar"
            variant="text"
            size="small"
            v-bind="activatorProps"
          />
        </template>
      </v-text-field>
    </template>

    <v-date-picker
      v-model="pickerValue"
      :first-day-of-week="1"
      show-adjacent-months
    />
  </v-menu>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { formatDate } from "@/lib/datetime";
import { formatDateOnly, parseDateOnly } from "@/lib/vacation-dates";

type Rule = (value: unknown) => boolean | string;

const props = withDefaults(
  defineProps<{
    modelValue: string;
    label: string;
    rules?: Rule[];
  }>(),
  {
    modelValue: "",
    rules: () => [],
  },
);

const emit = defineEmits<{ (event: "update:modelValue", value: string): void }>();
const { t } = useI18n();

const menuOpen = ref(false);
const formattedValue = ref("");

watch(
  () => props.modelValue,
  (value) => {
    formattedValue.value = value ? formatDate(value) : "";
  },
  { immediate: true },
);

const pickerValue = computed({
  get: () => {
    if (!props.modelValue) {
      return null;
    }
    return parseDateOnly(props.modelValue);
  },
  set: (value: unknown) => {
    const isoDate = normalizeToIsoDate(value);
    if (!isoDate) {
      return;
    }
    emit("update:modelValue", isoDate);
    formattedValue.value = formatDate(isoDate);
    menuOpen.value = false;
  },
});

const formatRule: Rule = (value) => {
  if (typeof value !== "string" || !value) {
    return true;
  }
  return parseFormattedDate(value) ? true : t("Дата в формате ДД.ММ.ГГ");
};

const fieldRules = computed<Rule[]>(() => [...props.rules, formatRule]);

function parseFormattedDate(value: string): Date | null {
  const match = value.trim().match(/^(\d{2})\.(\d{2})\.(\d{4})$/);
  if (!match) {
    return null;
  }
  const day = Number(match[1]);
  const month = Number(match[2]);
  const year = Number(match[3]);
  const parsed = new Date(year, month - 1, day);
  if (Number.isNaN(parsed.getTime())) {
    return null;
  }
  if (
    parsed.getFullYear() !== year ||
    parsed.getMonth() !== month - 1 ||
    parsed.getDate() !== day
  ) {
    return null;
  }
  return parsed;
}

function normalizeToIsoDate(value: unknown): string | null {
  const picked = Array.isArray(value) ? value[0] : value;
  if (typeof picked === "string") {
    const parsed = parseDateOnly(picked);
    return parsed ? formatDateOnly(parsed) : null;
  }
  if (picked instanceof Date && !Number.isNaN(picked.getTime())) {
    return formatDateOnly(picked);
  }
  return null;
}

function handleTextUpdated(value: string) {
  if (!value) {
    emit("update:modelValue", "");
    return;
  }
  const parsed = parseFormattedDate(value);
  if (!parsed) {
    return;
  }
  emit("update:modelValue", formatDateOnly(parsed));
}

function clearValue() {
  emit("update:modelValue", "");
}
</script>
