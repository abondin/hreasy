<template>
  <v-card data-testid="shared-articles-card">
    <v-card-item>
      <template #prepend>
        <v-btn
          variant="text"
          size="small"
          :disabled="loading || articles.length <= 1"
          @click="prev"
        >
          <v-icon icon="mdi-chevron-left" />
          {{ prevArticleName }}
        </v-btn>
      </template>
      <template #title>
        <span>{{ currentArticle?.name }}</span>
      </template>
      <template #append>
        <v-btn
          variant="text"
          size="small"
          :disabled="loading || articles.length <= 1"
          @click="next"
        >
          {{ nextArticleName }}
          <v-icon icon="mdi-chevron-right" />
        </v-btn>
      </template>
    </v-card-item>

    <v-card-text>
      <v-alert
        v-if="error"
        type="error"
        variant="tonal"
        border="start"
      >
        {{ error }}
      </v-alert>
      <v-skeleton-loader v-else-if="loading" type="card" />
      <markdown-text-renderer
        v-else-if="currentArticle"
        :content="currentArticle.content"
      />
      <v-alert
        v-else
        type="info"
        variant="tonal"
        border="start"
      >
        {{ t("Отсутствуют данные") }}
      </v-alert>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import MarkdownTextRenderer from "@/components/shared/MarkdownTextRenderer.vue";
import { errorUtils } from "@/lib/errors";
import { listSharedArticles, type Article } from "@/services/article.service";

const { t } = useI18n();

const loading = ref(false);
const error = ref("");
const articles = ref<Article[]>([]);
const selectedIndex = ref(-1);

const currentArticle = computed(() => {
  if (selectedIndex.value < 0) {
    return null;
  }
  return articles.value[selectedIndex.value] ?? null;
});
const prevArticleName = computed(() => getAdjacentArticleName(-1));
const nextArticleName = computed(() => getAdjacentArticleName(1));

async function load(): Promise<void> {
  loading.value = true;
  error.value = "";
  selectedIndex.value = -1;
  try {
    articles.value = await listSharedArticles();
    if (articles.value.length > 0) {
      selectedIndex.value = 0;
    }
  } catch (loadError) {
    error.value = errorUtils.shortMessage(loadError);
  } finally {
    loading.value = false;
  }
}

function prev(): void {
  if (articles.value.length <= 1) {
    return;
  }
  selectedIndex.value = selectedIndex.value <= 0 ? articles.value.length - 1 : selectedIndex.value - 1;
}

function next(): void {
  if (articles.value.length <= 1) {
    return;
  }
  selectedIndex.value = selectedIndex.value >= articles.value.length - 1 ? 0 : selectedIndex.value + 1;
}

function getAdjacentArticleName(direction: -1 | 1): string {
  if (articles.value.length <= 1 || selectedIndex.value < 0) {
    return "";
  }
  const nextIndex = direction === -1
    ? (selectedIndex.value <= 0 ? articles.value.length - 1 : selectedIndex.value - 1)
    : (selectedIndex.value >= articles.value.length - 1 ? 0 : selectedIndex.value + 1);
  const article = articles.value[nextIndex];
  return truncate(article?.name ?? "", 30);
}

function truncate(value: string, maxLength: number): string {
  return value.length > maxLength ? `${value.slice(0, maxLength - 1)}...` : value;
}

void load();
</script>
