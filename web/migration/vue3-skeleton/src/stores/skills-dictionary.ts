import { defineStore } from "pinia";
import { readonly, ref } from "vue";
import type {
  SharedSkillName,
  SkillGroup,
} from "@/services/skills.service";
import {
  fetchSharedSkillNames,
  fetchSkillGroups,
} from "@/services/skills.service";

export const useSkillsDictionaryStore = defineStore(
  "skills-dictionary",
  () => {
    const skillGroups = ref<SkillGroup[]>([]);
    const sharedSkillNames = ref<SharedSkillName[]>([]);
    const loading = ref(false);
    const loaded = ref(false);
    const error = ref<unknown>(null);

    async function loadSkillMetadata(force = false) {
      if (loading.value) {
        return;
      }
      if (loaded.value && !force) {
        return;
      }
      loading.value = true;
      error.value = null;
      try {
        const [groups, names] = await Promise.all([
          fetchSkillGroups(),
          fetchSharedSkillNames(),
        ]);
        skillGroups.value = groups;
        sharedSkillNames.value = names;
        loaded.value = true;
      } catch (err) {
        error.value = err;
        throw err;
      } finally {
        loading.value = false;
      }
    }

    return {
      skillGroups: readonly(skillGroups),
      sharedSkillNames: readonly(sharedSkillNames),
      loading: readonly(loading),
      error: readonly(error),
      loaded: readonly(loaded),
      loadSkillMetadata,
    };
  },
);
