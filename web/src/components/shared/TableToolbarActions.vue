<template>
  <div class="d-flex align-center ga-2">
    <v-tooltip v-if="showRefresh" location="bottom">
      <template #activator="{ props }">
        <v-btn
          v-bind="props"
          icon="mdi-refresh"
          variant="text"
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
          icon="mdi-plus"
          color="primary"
          variant="text"
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
          icon="mdi-file-excel"
          variant="text"
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
