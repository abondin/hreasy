<!--
  Skills list rendered without its own card wrapper so parent layouts can style it freely.
  Shows grouped chips with rating details and exposes add/delete hooks.
-->
<template>
  <div class="profile-skills-card">
    <v-skeleton-loader v-if="loading" type="chip" class="my-2"/>

    <v-alert
        v-else-if="errorMessage"
        type="error"
        variant="tonal"
        border="start"
        density="comfortable"
    >
      {{ errorMessage }}
    </v-alert>

    <div v-else class="profile-skills-card__layout">
      <div class="profile-skills-card__list">
        <div v-if="groupedSkills.length === 0 && canAdd" class="profile-skills-card__actions mt-2">
          <v-tooltip location="top">
            <template #activator="{ props: tooltipProps }">
              <v-btn
                  v-bind="tooltipProps"
                  icon="mdi-plus"
                  color="primary"
                  variant="tonal"
                  @click="emitAdd"
              />
            </template>
            {{ t("Добавить навык") }}
          </v-tooltip>
        </div>

        <v-row v-else :dense="isDense">
            <v-col
                v-for="group in groupedSkills"
                :key="group.group.id"
                cols="auto"
                class="profile-skills-card__group"
            >
              <div
                :class="[
                  'font-weight-medium',
                  isDense ? 'mb-1' : 'mb-2',
                ]"
              >
                {{ group.group.name }}
              </div>
              <div class="profile-skills-card__skills-grid">
                <v-menu
                    v-for="skill in group.skills"
                    :key="skill.id"
                    :model-value="isMenuOpen(skill.id)"
                    location="bottom"
                    :close-on-content-click="false"
                    @update:model-value="(value) => onMenuToggle(skill.id, value)"
                >
                  <template #activator="{ props: menuProps }">
                    <v-chip
                        v-bind="menuProps"
                        variant="outlined"
                        class="profile-skills-card__chip ma-0"
                    >
                      <span class="profile-skills-card__chip-name" :title="skill.name">
                        {{ skill.name }}
                      </span>
                    </v-chip>
                  </template>

                    <v-sheet class="profile-skills-card__menu pa-4" width="320">
                    <div class="profile-skills-card__menu-header">
                      <div class="profile-skills-card__menu-title">
                        {{ skill.name }}
                      </div>
                      <v-btn
                        icon="mdi-close"
                        size="small"
                        variant="text"
                        class="profile-skills-card__menu-close"
                        @click.stop.prevent="() => onMenuToggle(skill.id, false)"
                      />
                    </div>
                      <v-rating
                        half-increments
                        hover
                        color="amber"
                        :model-value="skill.ratings.myRating ?? 0"
                        :disabled="!canRate"
                        :size="isDense ? 18 : 24"
                        @update:model-value="(value) => emitRate(skill, value)"
                    />

                    <ul class="profile-skills-card__rating-stats">
                      <li>
                        {{ t("Мой рейтинг") }}:
                        {{ skill.ratings.myRating ?? t("Не задан") }}
                      </li>
                      <li>
                        {{ t("Средний рейтинг") }}:
                        <template v-if="skill.ratings.averageRating">
                          {{ skill.ratings.averageRating }}
                          ({{ ratingCountLabel(skill.ratings.ratingsCount ?? 0) }})
                        </template>
                        <template v-else>
                          {{ t("Нет оценок") }}
                        </template>
                      </li>
                    </ul>
                    <div v-if="canDelete" class="profile-skills-card__menu-actions">
                      <v-btn
                        color="error"
                        variant="text"
                        prepend-icon="mdi-delete"
                        @click.stop.prevent="() => emitDelete(skill)"
                      >
                        {{ t("Удалить") }}
                      </v-btn>
                    </div>
                  </v-sheet>
                </v-menu>
              </div>
            </v-col>
            <v-col v-if="canAdd" cols="auto" class="profile-skills-card__actions">
              <v-tooltip location="top">
                <template #activator="{ props: tooltipProps }">
                  <v-btn
                      v-bind="tooltipProps"
                      icon="mdi-plus"
                      color="primary"
                      variant="tonal"
                      @click="emitAdd"
                  />
                </template>
                {{ t("Добавить навык") }}
              </v-tooltip>
            </v-col>
          </v-row>
        </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import {computed, ref, watch} from "vue";
import {useI18n} from "vue-i18n";
import type {GroupedSkillsItem} from "@/composables/useEmployeeSkills";
import type {Skill} from "@/services/skills.service";

const props = defineProps<{
  groupedSkills: GroupedSkillsItem[];
  loading?: boolean;
  error?: unknown;
  canEdit: boolean;
  canAdd?: boolean;
  canDelete?: boolean;
  canRate?: boolean;
  dense?: boolean;
}>();

const emit = defineEmits<{
  (event: "add"): void;
  (event: "rate", payload: { skill: Skill; rating: number }): void;
  (event: "delete", skill: Skill): void;
}>();

const {t} = useI18n();
const activeSkillId = ref<number | null>(null);
const canAdd = computed(() => props.canAdd ?? props.canEdit);
const canDelete = computed(() => props.canDelete ?? props.canEdit);
const canRate = computed(() => props.canRate ?? props.canEdit);
const isDense = computed(() => Boolean(props.dense));

const errorMessage = computed(() => {
  if (!props.error) {
    return "";
  }
  if (props.error instanceof Error) {
    return props.error.message;
  }
  return String(props.error);
});

watch(
    () => props.groupedSkills,
    () => {
      if (
          activeSkillId.value &&
          !props.groupedSkills.some((group) =>
              group.skills.some((skill) => skill.id === activeSkillId.value),
          )
      ) {
        activeSkillId.value = null;
      }
    },
    {deep: true},
);

function emitAdd() {
  emit("add");
}

function emitRate(
    skill: Skill,
    value: number | string | null | undefined,
) {
  if (!canRate.value) {
    return;
  }
  const numericValue =
      typeof value === "number" ? value : Number.parseFloat(String(value));
  if (!Number.isFinite(numericValue) || numericValue <= 0) {
    return;
  }
  emit("rate", {skill, rating: numericValue});
}

function emitDelete(skill: Skill) {
  if (!canDelete.value) {
    return;
  }
  emit("delete", skill);
}

function onMenuToggle(skillId: number, open: boolean) {
  activeSkillId.value = open ? skillId : null;
}

function isMenuOpen(skillId: number): boolean {
  return activeSkillId.value === skillId;
}

function ratingCountLabel(count: number): string {
  return t("ratingCounts", {plural: count, n: count}) as string;
}
</script>

<style scoped>
.profile-skills-card {
  width: 100%;
}

.profile-skills-card__layout {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
}

.profile-skills-card__list {
  flex: 1 1 auto;
  min-width: 0;
}

.profile-skills-card__empty {
  color: rgba(0, 0, 0, 0.6);
}

.profile-skills-card__group {
  min-width: 220px;
}

.profile-skills-card__skills-grid {
  display: grid;
  grid-template-columns: repeat(3, max-content);
  gap: 6px;
  align-items: start;
}

.profile-skills-card__actions {
  align-self: center;
}

.profile-skills-card__chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  max-width: 360px;
}

.profile-skills-card__chip-name {
  min-width: 0;
  max-width: 180px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.profile-skills-card__chip-rating {
  font-size: 0.85rem;
  white-space: nowrap;
  color: rgba(0, 0, 0, 0.54);
}

.profile-skills-card__chip-close {
  min-width: auto;
}

.profile-skills-card__rating-stats {
  padding-left: 16px;
  margin: 12px 0 0;
}

.profile-skills-card__rating-stats li {
  margin-bottom: 4px;
}

.profile-skills-card__menu {
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.profile-skills-card__menu-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.profile-skills-card__menu-title {
  font-weight: 600;
  margin-bottom: 8px;
  word-break: break-word;
}

.profile-skills-card__menu-close {
  min-width: auto;
}

.profile-skills-card__menu-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
