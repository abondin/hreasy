<template>
  <TableFirstPageLayout test-id="admin-articles-view">
    <v-card class="d-flex flex-column h-100" data-testid="admin-articles-card">
      <HREasyTableBase
        table-class="admin-articles-table text-truncate"
        :headers="headers"
        :items="filteredItems"
        height="fill"
        :fixed-header="true"
        density="compact"
        :loading="loading"
        :loading-text="t('Загрузка_данных')"
        :no-data-text="t('Отсутствуют данные')"
        :row-props="rowProps"
        hover
        @click:row="onClickRow"
      >
        <template #filters>
          <v-card-text class="pt-4 pb-2">
            <AdaptiveFilterBar :items="filterBarItems" :has-right-actions="true">
              <template #left-actions>
                <table-toolbar-actions
                  :disabled="loading"
                  show-refresh
                  :refresh-label="t('Обновить данные')"
                  @refresh="load"
                />
              </template>

              <template #filter-search>
                <v-text-field
                  v-model="filter.search"
                  :label="t('Поиск')"
                  prepend-inner-icon="mdi-magnify"
                  variant="outlined"
                  density="compact"
                  hide-details
                  clearable
                />
              </template>

              <template #filter-group>
                <v-select
                  v-model="filter.articleGroup"
                  :items="groupOptions"
                  item-title="title"
                  item-value="value"
                  :label="t('Группа')"
                  variant="outlined"
                  density="compact"
                  multiple
                  clearable
                  hide-details
                >
                  <template #selection="{ item, index }">
                    <CollapsedSelectionContent
                      :index="index"
                      :total="filter.articleGroup.length"
                      :label="getFilterSelectionLabel(item)"
                    />
                  </template>
                </v-select>
              </template>

              <template #filter-moderated>
                <v-select
                  v-model="filter.onlyModerated"
                  :items="booleanOptions"
                  item-title="title"
                  item-value="value"
                  :label="t('Только модерированные')"
                  variant="outlined"
                  density="compact"
                  clearable
                  hide-details
                />
              </template>

              <template #filter-archived>
                <v-select
                  v-model="filter.showArchived"
                  :items="booleanOptions"
                  item-title="title"
                  item-value="value"
                  :label="t('Показать архив')"
                  variant="outlined"
                  density="compact"
                  clearable
                  hide-details
                />
              </template>

              <template #right-actions>
                <table-toolbar-actions
                  :disabled="loading"
                  show-add
                  :add-label="t('Добавить')"
                  @add="openCreate"
                />
              </template>
            </AdaptiveFilterBar>
          </v-card-text>
        </template>

        <template #before-table>
          <v-alert v-if="error" type="error" variant="tonal" border="start" class="mb-3">
            {{ error }}
          </v-alert>
        </template>

        <template #[`item.moderated`]="{ item }">
          {{ item.moderated ? t("Да") : t("Нет") }}
        </template>
        <template #[`item.archived`]="{ item }">
          {{ item.archived ? t("Да") : t("Нет") }}
        </template>
        <template #[`item.articleGroup`]="{ item }">
          {{ t(`ARTICLE_GROUP.${item.articleGroup}`) }}
        </template>
        <template #[`item.updatedAt`]="{ item }">
          {{ formatDateTime(item.updatedAt) }}
        </template>
      </HREasyTableBase>
    </v-card>

    <v-dialog v-model="dialog" persistent max-width="1080">
      <admin-article-form
        :input="current"
        @close="dialog = false"
        @saved="onSaved"
      />
    </v-dialog>
  </TableFirstPageLayout>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from "vue";
import TableFirstPageLayout from "@/components/shared/TableFirstPageLayout.vue";
import { useI18n } from "vue-i18n";
import AdaptiveFilterBar from "@/components/shared/AdaptiveFilterBar.vue";
import CollapsedSelectionContent from "@/components/shared/CollapsedSelectionContent.vue";
import HREasyTableBase from "@/components/shared/HREasyTableBase.vue";
import TableToolbarActions from "@/components/shared/TableToolbarActions.vue";
import { extractDataTableRow } from "@/lib/data-table";
import { formatDateTime } from "@/lib/datetime";
import { errorUtils } from "@/lib/errors";
import { ALL_ARTICLES_GROUPS } from "@/services/article.service";
import {
  listAdminArticles,
  type ArticleFull,
} from "@/services/admin/admin-article.service";
import AdminArticleForm from "@/views/admin/articles/components/AdminArticleForm.vue";

const { t } = useI18n();

const loading = ref(false);
const dialog = ref(false);
const error = ref("");
const items = ref<ArticleFull[]>([]);
const current = ref<ArticleFull | null>(null);

const filter = reactive({
  search: "",
  articleGroup: [] as string[],
  onlyModerated: null as boolean | null,
  showArchived: null as boolean | null,
});

function getFilterSelectionLabel(item: unknown): string {
  if (typeof item === "string") {
    return item;
  }
  if (item && typeof item === "object" && "title" in item && typeof item.title === "string") {
    return item.title;
  }
  return "";
}

const filterBarItems = computed(() => [
  { id: "search", minWidth: 380, active: filter.search.trim().length > 0, grow: true },
  { id: "group", minWidth: 280, active: filter.articleGroup.length > 0 },
  { id: "moderated", minWidth: 220, active: filter.onlyModerated !== null },
  { id: "archived", minWidth: 220, active: filter.showArchived !== null },
]);

const booleanOptions = computed(() => [
  { title: t("Нет"), value: false },
  { title: t("Да"), value: true },
]);
const groupOptions = computed(() =>
  ALL_ARTICLES_GROUPS.map((value) => ({
    value,
    title: t(`ARTICLE_GROUP.${value}`),
  })),
);
const headers = computed(() => [
  { title: t("Статья"), key: "name", width: "320px" },
  { title: t("Группа"), key: "articleGroup", width: "180px" },
  { title: t("Модерированная"), key: "moderated", width: "150px" },
  { title: t("Архивная"), key: "archived", width: "140px" },
  { title: t("Обновлена"), key: "updatedAt", width: "190px" },
]);

const filteredItems = computed(() => {
  const query = filter.search.trim().toLowerCase();
  return items.value.filter((item) => {
    if (query && !item.name.toLowerCase().includes(query)) {
      return false;
    }
    if (filter.articleGroup.length > 0 && !filter.articleGroup.includes(item.articleGroup)) {
      return false;
    }
    if (filter.onlyModerated === true && !item.moderated) {
      return false;
    }
    if (filter.showArchived !== true && item.archived) {
      return false;
    }
    return true;
  });
});

function rowProps() {
  return { class: "cursor-pointer" };
}

async function load(): Promise<void> {
  loading.value = true;
  error.value = "";
  try {
    items.value = await listAdminArticles();
  } catch (loadError) {
    error.value = errorUtils.shortMessage(loadError);
  } finally {
    loading.value = false;
  }
}

function openCreate(): void {
  current.value = null;
  dialog.value = true;
}

function onClickRow(_event: Event, row: unknown): void {
  const item = extractDataTableRow<ArticleFull>(row);
  if (!item) {
    return;
  }
  current.value = item;
  dialog.value = true;
}

function onSaved(): void {
  dialog.value = false;
  void load();
}

void load();
</script>

<style scoped>
.admin-articles-table :deep(tbody tr:hover) {
  cursor: pointer;
}
</style>
