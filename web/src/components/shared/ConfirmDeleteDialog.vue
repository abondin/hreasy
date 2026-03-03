<!--
  Generic confirmation dialog for delete actions.
-->
<template>
  <v-dialog
    :model-value="open"
    max-width="480"
    persistent
    @update:model-value="onModelChange"
  >
    <v-card>
      <v-card-title>{{ title }}</v-card-title>
      <v-card-text>
        <p>{{ message }}</p>
        <p v-if="itemLabel" class="font-weight-medium">
          {{ itemLabel }}
        </p>
        <v-alert
          v-if="errorMessage"
          type="error"
          variant="tonal"
          border="start"
          class="mt-2"
        >
          {{ errorMessage }}
        </v-alert>
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn
          variant="text"
          color="secondary"
          :disabled="loading"
          @click="emitClose"
        >
          {{ cancelLabel }}
        </v-btn>
        <v-btn
          color="primary"
          :loading="loading"
          :disabled="loading"
          @click="emitConfirm"
        >
          {{ confirmLabel }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    open: boolean;
    title: string;
    message: string;
    itemLabel?: string | null;
    confirmLabel?: string;
    cancelLabel?: string;
    loading?: boolean;
    errorMessage?: string;
  }>(),
  {
    itemLabel: null,
    confirmLabel: "Да",
    cancelLabel: "Нет",
    loading: false,
    errorMessage: "",
  },
);

const emit = defineEmits<{
  (event: "close"): void;
  (event: "confirm"): void;
}>();

function emitClose() {
  emit("close");
}

function emitConfirm() {
  emit("confirm");
}

function onModelChange(value: boolean) {
  if (!value) {
    emitClose();
  }
}
</script>
