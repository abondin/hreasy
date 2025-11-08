<!--
  Confirmation dialog for removing a skill from employee profile.
  Delegates deletion to provided handler so parent can update state.
  -->
<template>
  <!--<editor-fold desc="Delete skill dialog">-->
  <v-dialog
    :model-value="open"
    max-width="480"
    persistent
    @update:model-value="onModelChange"
  >
    <v-card>
      <v-card-title>
        {{ t("Удаление") }}
      </v-card-title>
      <v-card-text>
        <p>
          {{ t("Вы уверены, что хотите удалить запись?") }}
        </p>
        <p class="font-weight-medium">
          {{ skillName }}
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
          {{ t("Нет") }}
        </v-btn>
        <v-btn
          color="primary"
          :loading="loading"
          :disabled="loading"
          @click="confirm"
        >
          {{ t("Да") }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
  <!-- </editor-fold> -->
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { errorUtils } from "@/lib/errors";
import type { Skill } from "@/services/skills.service";

const props = defineProps<{
  open: boolean;
  skill: Skill | null;
  deleteSkill: (skill: Skill) => Promise<void>;
}>();

const emit = defineEmits<{ (event: "close"): void; (event: "deleted"): void }>();

const { t } = useI18n();
const loading = ref(false);
const errorMessage = ref("");

const skillName = computed(() => props.skill?.name ?? "");

watch(
  () => props.open,
  (value) => {
    if (!value) {
      errorMessage.value = "";
      loading.value = false;
    }
  },
);

function emitClose() {
  emit("close");
}

function onModelChange(value: boolean) {
  if (!value) {
    emitClose();
  }
}

async function confirm() {
  if (!props.skill || loading.value) {
    return;
  }
  loading.value = true;
  errorMessage.value = "";
  try {
    await props.deleteSkill(props.skill);
    emit("deleted");
  } catch (error) {
    errorMessage.value = errorUtils.shortMessage(error);
  } finally {
    loading.value = false;
  }
}
</script>
