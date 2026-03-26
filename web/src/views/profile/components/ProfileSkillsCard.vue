<!--
  Skills list rendered without its own card wrapper so parent layouts can style it freely.
  Shows grouped chips with rating details and exposes add/delete hooks.
-->
<template>
  <div class="w-100">
    <v-skeleton-loader v-if="loading" type="chip" class="my-2" />

    <v-alert
      v-else-if="errorMessage"
      type="error"
      variant="tonal"
      border="start"
      density="comfortable"
    >
      {{ errorMessage }}
    </v-alert>

    <div v-else>
      <div v-if="groupedSkills.length === 0 && canAdd" class="mt-2">
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

      <v-row v-else :density="isDense ? 'comfortable' : 'default'" align="start">
        <v-col
          v-for="group in groupedSkills"
          :key="group.group.id"
          cols="12"
          sm="6"
          lg="4"
        >
          <div :class="['font-weight-medium', isDense ? 'mb-1' : 'mb-2']">
            {{ group.group.name }}
          </div>

          <div class="d-flex flex-wrap align-start ga-2">
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
                  class="ma-0"
                >
                  <span class="d-inline-block text-truncate" style="max-width: 180px">
                    {{ skill.name }}
                  </span>
                </v-chip>
              </template>

              <v-card width="320" elevation="4">
                <v-card-title class="d-flex align-center justify-space-between ga-2 text-body-1 font-weight-medium">
                  <span class="text-break">{{ skill.name }}</span>
                  <v-btn
                    icon="mdi-close"
                    size="small"
                    variant="text"
                    @click.stop.prevent="() => onMenuToggle(skill.id, false)"
                  />
                </v-card-title>

                <v-card-text>
                  <v-rating
                    half-increments
                    hover
                    color="amber"
                    :model-value="skill.ratings.myRating ?? 0"
                    :disabled="!canRate"
                    :size="isDense ? 18 : 24"
                    @update:model-value="(value) => emitRate(skill, value)"
                  />

                  <v-list density="compact" bg-color="transparent" class="px-0 mt-2">
                    <v-list-item class="px-0 min-h-0">
                      <template #title>
                        <span>
                          {{ t("Мой рейтинг") }}:
                          {{ skill.ratings.myRating ?? t("Не задан") }}
                        </span>
                      </template>
                    </v-list-item>
                    <v-list-item class="px-0 min-h-0">
                      <template #title>
                        <span>
                          {{ t("Средний рейтинг") }}:
                          <template v-if="skill.ratings.averageRating">
                            {{ skill.ratings.averageRating }}
                            ({{ ratingCountLabel(skill.ratings.ratingsCount ?? 0) }})
                          </template>
                          <template v-else>
                            {{ t("Нет оценок") }}
                          </template>
                        </span>
                      </template>
                    </v-list-item>
                  </v-list>
                </v-card-text>

                <v-card-actions v-if="canDelete" class="justify-end">
                  <v-btn
                    color="error"
                    variant="text"
                    prepend-icon="mdi-delete"
                    @click.stop.prevent="() => emitDelete(skill)"
                  >
                    {{ t("Удалить") }}
                  </v-btn>
                </v-card-actions>
              </v-card>
            </v-menu>
          </div>
        </v-col>

        <v-col v-if="canAdd" cols="auto" class="align-self-center">
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
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import type { GroupedSkillsItem } from "@/lib/skills";
import type { Skill } from "@/services/skills.service";

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

const { t } = useI18n();
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
  { deep: true },
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
  emit("rate", { skill, rating: numericValue });
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
  return t("ratingCounts", { plural: count, n: count }) as string;
}
</script>