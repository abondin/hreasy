<template>
  <v-container class="py-6" data-testid="admin-articles-view">
    <v-card data-testid="admin-articles-card">
      <HREasyTableBase
        table-class="admin-articles-table text-truncate"
        :headers="headers"
        :items="filteredItems"
        height="70vh"
        :fixed-header="true"
        density="compact"
        :loading="loading"
        :loading-text="t('\u0417\u0430\u0433\u0440\u0443\u0437\u043a\u0430_\u0434\u0430\u043d\u043d\u044b\u0445')"
        :no-data-text="t('\u041e\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u044e\u0442 \u0434\u0430\u043d\u043d\u044b\u0435')"
        :row-props="rowProps"
        hover
        @click:row="onClickRow"
      >
        <template #filters>
          <v-card-title class="d-flex ga-2 align-center flex-wrap">
            <v-btn
              icon="mdi-refresh"
              variant="text"
              :loading="loading"
              data-testid="admin-articles-refresh"
              @click="load"
            />
            <v-btn
              icon="mdi-plus"
              color="primary"
              variant="text"
              :disabled="loading"
              data-testid="admin-articles-add"
              @click="openCreate"
            />
          </v-card-title>

          <v-card-text class="pb-0">
            <v-row density="comfortable">
              <v-col cols="12" lg="4">
                <v-text-field
                  v-model="filter.search"
                  :label="t('\u041f\u043e\u0438\u0441\u043a')"
                  append-inner-icon="mdi-magnify"
                  variant="outlined"
                  density="compact"
                  hide-details
                  clearable
                />
              </v-col>
              <v-col cols="12" lg="3">
                <v-select
                  v-model="filter.articleGroup"
                  :items="groupOptions"
                  item-title="title"
                  item-value="value"
                  :label="t('\u0413\u0440\u0443\u043f\u043f\u0430')"
                  variant="outlined"
                  density="compact"
                  multiple
                  chips
                  clearable
                  hide-details
                />
              </v-col>
              <v-col cols="12" lg="2">
                <v-select
                  v-model="filter.onlyModerated"
                  :items="booleanOptions"
                  item-title="title"
                  item-value="value"
                  :label="t('\u0422\u043e\u043b\u044c\u043a\u043e \u043c\u043e\u0434\u0435\u0440\u0438\u0440\u043e\u0432\u0430\u043d\u043d\u044b\u0435')"
                  variant="outlined"
                  density="compact"
                  clearable
                  hide-details
                />
              </v-col>
              <v-col cols="12" lg="3">
                <v-select
                  v-model="filter.showArchived"
                  :items="booleanOptions"
                  item-title="title"
                  item-value="value"
                  :label="t('\u041f\u043e\u043a\u0430\u0437\u0430\u0442\u044c \u0430\u0440\u0445\u0438\u0432')"
                  variant="outlined"
                  density="compact"
                  clearable
                  hide-details
                />
              </v-col>
            </v-row>
          </v-card-text>
        </template>

        <template #before-table>
          <v-alert v-if="error" type="error" variant="tonal" border="start" class="mb-3">
            {{ error }}
          </v-alert>
        </template>

        <template #[`item.moderated`]="{ item }">
          {{ item.moderated ? t("\u0414\u0430") : t("\u041d\u0435\u0442") }}
        </template>
        <template #[`item.archived`]="{ item }">
          {{ item.archived ? t("\u0414\u0430") : t("\u041d\u0435\u0442") }}
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
  </v-container>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from "vue";
import { useI18n } from "vue-i18n";
import HREasyTableBase from "@/components/shared/HREasyTableBase.vue";
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

const booleanOptions = computed(() => [
  { title: t("\u041d\u0435\u0442"), value: false },
  { title: t("\u0414\u0430"), value: true },
]);
const groupOptions = computed(() =>
  ALL_ARTICLES_GROUPS.map((value) => ({
    value,
    title: t(`ARTICLE_GROUP.${value}`),
  })),
);
const headers = computed(() => [
  { title: t("\u0421\u0442\u0430\u0442\u044c\u044f"), key: "name", width: "320px" },
  { title: t("\u0413\u0440\u0443\u043f\u043f\u0430"), key: "articleGroup", width: "180px" },
  { title: t("\u041c\u043e\u0434\u0435\u0440\u0438\u0440\u043e\u0432\u0430\u043d\u043d\u0430\u044f"), key: "moderated", width: "150px" },
  { title: t("\u0410\u0440\u0445\u0438\u0432\u043d\u0430\u044f"), key: "archived", width: "140px" },
  { title: t("\u041e\u0431\u043d\u043e\u0432\u043b\u0435\u043d\u0430"), key: "updatedAt", width: "190px" },
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
