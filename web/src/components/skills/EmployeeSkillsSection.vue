<!--
  Shared skills section with add/rate/delete dialogs.
-->
<template>
  <profile-skills-card
    :grouped-skills="groupedSkills"
    :loading="sectionLoading"
    :error="sectionError"
    :can-edit="canEdit"
    :can-add="canAdd"
    :can-delete="canDelete"
    :can-rate="canRate"
    :dense="dense"
    @add="handleAddSkillRequested"
    @rate="handleRateSkill"
    @delete="handleDeleteSkillRequested"
  />

  <skill-create-dialog
    :open="createSkillDialogOpen"
    :skill-groups="skillGroups"
    :shared-skill-names="sharedSkillNames"
    :submit-skill="submitSkill"
    @close="createSkillDialogOpen = false"
  />
  <skill-delete-dialog
    :open="deleteSkillDialogOpen"
    :skill="skillPendingDeletion"
    :delete-skill="confirmDeleteSkill"
    @close="closeDeleteDialog"
    @deleted="handleSkillDeleted"
  />
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { storeToRefs } from "pinia";
import ProfileSkillsCard from "@/views/profile/components/ProfileSkillsCard.vue";
import SkillCreateDialog from "@/views/profile/components/SkillCreateDialog.vue";
import SkillDeleteDialog from "@/views/profile/components/SkillDeleteDialog.vue";
import { useSkillsDictionaryStore } from "@/stores/skills-dictionary";
import type { GroupedSkillsItem } from "@/lib/skills";
import type { Skill, AddSkillBody } from "@/services/skills.service";

const props = defineProps<{
  groupedSkills: GroupedSkillsItem[];
  loading?: boolean;
  error?: unknown;
  canEdit: boolean;
  canAdd: boolean;
  canDelete: boolean;
  canRate: boolean;
  dense?: boolean;
  submitSkill: (payload: AddSkillBody) => Promise<Skill>;
  rateSkill: (payload: { skill: Skill; rating: number }) => Promise<void>;
  deleteSkill: (skill: Skill) => Promise<void>;
}>();

const emit = defineEmits<{
  (event: "deleted", skill: Skill): void;
}>();

const skillsDictionaryStore = useSkillsDictionaryStore();
const {
  skillGroups,
  sharedSkillNames,
  loading: skillsMetadataLoading,
  error: skillsMetadataError,
} = storeToRefs(skillsDictionaryStore);

const createSkillDialogOpen = ref(false);
const deleteSkillDialogOpen = ref(false);
const skillPendingDeletion = ref<Skill | null>(null);
const actionError = ref<unknown>(null);

const sectionLoading = computed(
  () => Boolean(props.loading) || skillsMetadataLoading.value,
);

const sectionError = computed(() => {
  return (
    actionError.value ??
    props.error ??
    skillsMetadataError.value ??
    null
  );
});

async function handleAddSkillRequested() {
  if (!props.canAdd) {
    return;
  }
  actionError.value = null;
  try {
    await skillsDictionaryStore.loadSkillMetadata();
    createSkillDialogOpen.value = true;
  } catch (error) {
    actionError.value = error;
  }
}

async function handleRateSkill(payload: { skill: Skill; rating: number }) {
  if (!props.canRate) {
    return;
  }
  actionError.value = null;
  try {
    await props.rateSkill(payload);
  } catch (error) {
    actionError.value = error;
  }
}

function handleDeleteSkillRequested(skill: Skill) {
  if (!props.canDelete) {
    return;
  }
  actionError.value = null;
  skillPendingDeletion.value = skill;
  deleteSkillDialogOpen.value = true;
}

async function confirmDeleteSkill(skill: Skill) {
  try {
    await props.deleteSkill(skill);
  } catch (error) {
    actionError.value = error;
    throw error;
  }
}

function handleSkillDeleted() {
  const skill = skillPendingDeletion.value;
  if (!skill) {
    return;
  }
  emit("deleted", skill);
  closeDeleteDialog();
}

function closeDeleteDialog() {
  deleteSkillDialogOpen.value = false;
  skillPendingDeletion.value = null;
}
</script>
