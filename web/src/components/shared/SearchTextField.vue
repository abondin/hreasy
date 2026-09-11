<template>
  <v-text-field
    :model-value="modelValue"
    :label="label"
    variant="outlined"
    density="compact"
    clearable
    hide-details
    :data-testid="testId"
    v-bind="$attrs"
    @update:model-value="$emit('update:modelValue', normalizeSearchInput($event))"
  >
    <template #prepend-inner>
      <v-menu :close-on-content-click="false">
        <template #activator="{ props }">
          <v-btn
            v-bind="props"
            icon
            variant="text"
            size="x-small"
            class="search-settings-button"
            :aria-label="t('Настройки поиска')"
          >
            <v-icon icon="mdi-magnify" />
            <v-icon icon="mdi-cog" size="11" class="search-settings-indicator" />
          </v-btn>
        </template>
        <v-list density="compact">
          <v-list-item
            v-for="option in options"
            :key="option.key"
            @click="toggle(option.key)"
          >
            <template #prepend>
              <v-checkbox-btn
                :model-value="settings[option.key]"
                @click.stop
                @update:model-value="update(option.key, Boolean($event))"
              />
            </template>
            <v-list-item-title>{{ option.title }}</v-list-item-title>
          </v-list-item>
        </v-list>
      </v-menu>
    </template>
  </v-text-field>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { normalizeSearchInput, type SearchSettings } from "@/lib/search";

defineOptions({ inheritAttrs: false });

const props = defineProps<{
  modelValue: string;
  settings: SearchSettings;
  label: string;
  testId?: string;
}>();
const emit = defineEmits<{
  (event: "update:modelValue", value: string): void;
  (event: "update:settings", value: SearchSettings): void;
}>();
const { t } = useI18n();
const options = computed<Array<{ key: keyof SearchSettings; title: string }>>(() => [
  { key: "fuzzy", title: t("Неточный поиск") },
  { key: "keyboardLayout", title: t("Исправлять раскладку") },
  { key: "unorderedTerms", title: t("Слова в любом порядке") },
]);

function update(key: keyof SearchSettings, value: boolean): void {
  emit("update:settings", { ...props.settings, [key]: value });
}

function toggle(key: keyof SearchSettings): void {
  update(key, !props.settings[key]);
}
</script>

<style scoped>
.search-settings-indicator {
  position: absolute;
  left: 0;
  bottom: 0;
}
</style>
