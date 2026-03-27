<template>
  <div class="d-flex align-center ga-2">
    <v-tooltip v-if="showRefresh" location="bottom">
      <template #activator="{ props }">
        <v-btn
          v-bind="props"
          data-testid="toolbar-refresh"
          icon="mdi-refresh"
          variant="text"
          size="small"
          density="comfortable"
          :disabled="disabled"
          @click="$emit('refresh')"
        />
      </template>
      <span>{{ refreshLabel }}</span>
    </v-tooltip>

    <v-tooltip v-if="showAdd" location="bottom">
      <template #activator="{ props }">
        <v-btn
          v-bind="props"
          data-testid="toolbar-add"
          icon="mdi-plus"
          color="primary"
          variant="text"
          size="small"
          density="comfortable"
          :disabled="disabled"
          @click="$emit('add')"
        />
      </template>
      <span>{{ addLabel }}</span>
    </v-tooltip>

    <v-tooltip v-if="showExport" location="bottom">
      <template #activator="{ props }">
        <v-btn
          v-bind="props"
          data-testid="toolbar-export"
          icon="mdi-file-excel"
          variant="text"
          size="small"
          density="comfortable"
          :disabled="disabled"
          @click="$emit('export')"
        />
      </template>
      <span>{{ exportLabel }}</span>
    </v-tooltip>
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    disabled?: boolean;
    showRefresh?: boolean;
    showAdd?: boolean;
    showExport?: boolean;
    refreshLabel?: string;
    addLabel?: string;
    exportLabel?: string;
  }>(),
  {
    disabled: false,
    showRefresh: false,
    showAdd: false,
    showExport: false,
    refreshLabel: "",
    addLabel: "",
    exportLabel: "",
  },
);

defineEmits<{
  refresh: [];
  add: [];
  export: [];
}>();
</script>
