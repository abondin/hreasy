<!--
  Date range selector with presets and explicit apply action.
-->
<template>
  <v-menu
    ref="menuRef"
    v-model="menuOpen"
    :close-on-content-click="false"
    transition="scale-transition"
    offset-y
    max-width="560"
  >
    <template #activator="{ props: activatorProps }">
      <v-text-field
        :model-value="displayValue"
        :label="label"
        prepend-inner-icon="mdi-calendar"
        readonly
        clearable
        density="compact"
        v-bind="activatorProps"
        @click:clear.stop="clearRange"
      />
    </template>

    <v-card class="pa-2">
      <v-date-picker
        v-model="pickerRange"
        multiple="range"
        :first-day-of-week="1"
        show-adjacent-months
        width="520"
      />
      <v-card-actions class="px-1 pt-1 pb-0">
        <div class="d-flex flex-wrap ga-2">
          <v-btn
            v-if="isShortcutAllowed('todayPlus5Days')"
            size="x-small"
            variant="text"
            @click="selectTodayPlus5Days"
          >
            {{ t("Текущая дата +5 дней") }}
          </v-btn>
          <v-btn
            v-if="isShortcutAllowed('month')"
            size="x-small"
            variant="text"
            @click="selectCurrentMonth"
          >
            {{ t("Текущий месяц") }}
          </v-btn>
          <v-btn
            v-if="isShortcutAllowed('week')"
            size="x-small"
            variant="text"
            @click="selectCurrentWeek"
          >
            {{ t("Текущая неделя") }}
          </v-btn>
          <v-btn
            v-if="isShortcutAllowed('year')"
            size="x-small"
            variant="text"
            @click="selectCurrentYear"
          >
            {{ t("Год") }}
          </v-btn>
        </div>
        <v-spacer />
        <v-btn variant="text" @click="menuOpen = false">
          {{ t("Закрыть") }}
        </v-btn>
        <v-btn color="primary" variant="text" @click="applyRange">
          {{ t("Применить") }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-menu>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { addDays, formatDateOnly, parseDateOnly } from "@/lib/vacation-dates";
import { formatDate } from "@/lib/datetime";

type AllowedShortCut = "year" | "month" | "week" | "todayPlus5Days";

const props = withDefaults(
  defineProps<{
    modelValue: string[];
    label: string;
    allowedShortCut?: AllowedShortCut[];
  }>(),
  {
    modelValue: () => [],
    allowedShortCut: () => ["year", "month", "todayPlus5Days"],
  },
);

const emit = defineEmits<{ (event: "update:modelValue", value: string[]): void }>();
const { t } = useI18n();

const menuOpen = ref(false);
const draftRange = ref<string[]>([]);

watch(menuOpen, (open) => {
  if (open) {
    draftRange.value = expandBoundaryRange(props.modelValue);
  }
});

const displayValue = computed(() => {
  const current = normalizeBoundaryRange(props.modelValue);
  if (current.length === 0) {
    return t("Не выбран");
  }
  const start = formatDate(current[0]);
  const end = current[1] ? formatDate(current[1]) : "";
  if (!end) {
    return `${t("с")} ${start}`;
  }
  return `${t("с")} ${start} ${t("по")} ${end}`;
});

const pickerRange = computed({
  get: () =>
    draftRange.value
      .map((value) => parseDateOnly(value))
      .filter((value): value is Date => Boolean(value)),
  set: (value: unknown) => {
    draftRange.value = normalizeSelection(value);
  },
});

function isShortcutAllowed(shortcut: AllowedShortCut) {
  return props.allowedShortCut.includes(shortcut);
}

function normalizeSelection(value: unknown): string[] {
  const asArray = Array.isArray(value) ? value : [];
  const normalized = asArray
    .map(normalizeToIsoDate)
    .filter((item): item is string => Boolean(item))
    .sort((left, right) => left.localeCompare(right))
    .filter((item, index, arr) => index === 0 || item !== arr[index - 1]);

  return normalized;
}

function normalizeBoundaryRange(value: unknown): string[] {
  const normalized = normalizeSelection(value);
  if (normalized.length === 0) {
    return [];
  }
  if (normalized.length === 1) {
    const single = normalized[0];
    return single ? [single] : [];
  }
  const start = normalized[0];
  const end = normalized[normalized.length - 1];
  if (!start || !end) {
    return [];
  }
  return [start, end];
}

function buildRangeSelection(start: Date, end: Date): string[] {
  const normalizedStart = new Date(start.getFullYear(), start.getMonth(), start.getDate());
  const normalizedEnd = new Date(end.getFullYear(), end.getMonth(), end.getDate());
  const from = normalizedStart <= normalizedEnd ? normalizedStart : normalizedEnd;
  const to = normalizedStart <= normalizedEnd ? normalizedEnd : normalizedStart;
  const selection: string[] = [];
  for (let cursor = new Date(from); cursor <= to; cursor = addDays(cursor, 1)) {
    selection.push(formatDateOnly(cursor));
  }
  return selection;
}

function expandBoundaryRange(value: unknown): string[] {
  const boundary = normalizeBoundaryRange(value);
  if (boundary.length === 0) {
    return [];
  }
  if (boundary.length === 1) {
    return boundary;
  }
  const start = parseDateOnly(boundary[0]);
  const end = parseDateOnly(boundary[1]);
  if (!start || !end) {
    return boundary;
  }
  return buildRangeSelection(start, end);
}

function normalizeToIsoDate(value: unknown): string | null {
  if (typeof value === "string") {
    const parsed = parseDateOnly(value);
    return parsed ? formatDateOnly(parsed) : null;
  }
  if (value instanceof Date && !Number.isNaN(value.getTime())) {
    return formatDateOnly(value);
  }
  return null;
}

function clearRange() {
  draftRange.value = [];
  emit("update:modelValue", []);
}

function applyRange() {
  emit("update:modelValue", normalizeBoundaryRange(draftRange.value));
  menuOpen.value = false;
}

function selectTodayPlus5Days() {
  const start = new Date();
  const end = addDays(start, 5);
  draftRange.value = buildRangeSelection(start, end);
}

function selectCurrentMonth() {
  const today = new Date();
  const start = new Date(today.getFullYear(), today.getMonth(), 1);
  const end = new Date(today.getFullYear(), today.getMonth() + 1, 0);
  draftRange.value = buildRangeSelection(start, end);
}

function selectCurrentWeek() {
  const today = new Date();
  const dayOfWeek = today.getDay();
  const offsetToMonday = dayOfWeek === 0 ? -6 : 1 - dayOfWeek;
  const start = addDays(today, offsetToMonday);
  const end = addDays(start, 6);
  draftRange.value = buildRangeSelection(start, end);
}

function selectCurrentYear() {
  const year = new Date().getFullYear();
  draftRange.value = buildRangeSelection(
    new Date(year, 0, 1),
    new Date(year, 11, 31),
  );
}
</script>
