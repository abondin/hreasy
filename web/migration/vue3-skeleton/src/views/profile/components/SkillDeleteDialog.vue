<!--
  Confirmation dialog for removing a skill from employee profile.
  Delegates deletion to provided handler so parent can update state.
  -->
<template>
  <!--<editor-fold desc="Delete skill dialog">-->
  <confirm-delete-dialog
    :open="open"
    :title="t('Удаление')"
    :message="t('Вы уверены, что хотите удалить запись?')"
    :item-label="skillName"
    :confirm-label="t('Да')"
    :cancel-label="t('Нет')"
    :loading="loading"
    :error-message="errorMessage"
    @close="emitClose"
    @confirm="confirm"
  />
  <!-- </editor-fold> -->
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { errorUtils } from "@/lib/errors";
import ConfirmDeleteDialog from "@/components/shared/ConfirmDeleteDialog.vue";
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
