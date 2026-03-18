<template>
  <v-card data-testid="admin-article-form">
    <v-card-title class="d-flex align-center">
      {{ input?.id ? t("Редактирование статьи") : t("Создание статьи") }}
      <v-spacer />
      <v-btn icon="mdi-close" variant="text" @click="$emit('close')" />
    </v-card-title>

    <v-card-text>
      <v-form ref="formRef" @submit.prevent="submit">
        <v-text-field
          v-model="form.name"
          :label="t('Название статьи')"
          :counter="255"
          :rules="nameRules"
          variant="outlined"
        />

        <v-select
          v-model="form.moderated"
          :items="booleanOptions"
          item-title="title"
          item-value="value"
          :label="t('Модерированная')"
          variant="outlined"
        />

        <markdown-text-editor
          v-model="form.content"
          :label="t('Содержимое статьи (Markdown)')"
          :rules="contentRules"
          :counter="20000"
        />

        <v-card v-if="articleId" variant="outlined" class="mt-4">
          <v-card-title class="text-subtitle-1">
            {{ t("\u0417\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c \u0438\u0437\u043e\u0431\u0440\u0430\u0436\u0435\u043d\u0438\u0435") }}
          </v-card-title>
          <v-card-text class="pt-0">
            <div class="text-body-2 text-medium-emphasis mb-4">
              {{ t("\u041f\u043e\u0434\u0434\u0435\u0440\u0436\u0438\u0432\u0430\u044e\u0442\u0441\u044f \u0442\u043e\u043b\u044c\u043a\u043e \u0438\u0437\u043e\u0431\u0440\u0430\u0436\u0435\u043d\u0438\u044f") }}
            </div>
            <v-btn
              color="primary"
              variant="tonal"
              prepend-icon="mdi-image-plus"
              :disabled="saving"
              @click="uploadDialog = true"
            >
              {{ t("\u0417\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c \u0438\u0437\u043e\u0431\u0440\u0430\u0436\u0435\u043d\u0438\u0435") }}
            </v-btn>
          </v-card-text>
        </v-card>

        <v-alert
          v-else
          type="info"
          variant="tonal"
          border="start"
          class="mt-4"
        >
          {{ t("\u0421\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u0435 \u0441\u0442\u0430\u0442\u044c\u044e, \u0437\u0430\u0442\u0435\u043c \u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u0435 \u0438\u0437\u043e\u0431\u0440\u0430\u0436\u0435\u043d\u0438\u0435") }}
        </v-alert>

        <v-select
          v-model="form.articleGroup"
          :items="groupOptions"
          item-title="title"
          item-value="value"
          :label="t('Группа')"
          :rules="groupRules"
          variant="outlined"
        />

        <v-select
          v-model="form.archived"
          :items="booleanOptions"
          item-title="title"
          item-value="value"
          :label="t('Архив')"
          variant="outlined"
        />
      </v-form>

      <v-alert
        v-if="error"
        type="error"
        variant="tonal"
        border="start"
        class="mt-4"
      >
        {{ error }}
      </v-alert>

      <v-alert
        v-if="uploadError"
        type="error"
        variant="tonal"
        border="start"
        class="mt-4"
      >
        {{ uploadError }}
      </v-alert>
    </v-card-text>
    <v-card-actions>
      <v-spacer />
      <v-btn variant="text" :disabled="saving" @click="$emit('close')">
        {{ t("Закрыть") }}
      </v-btn>
      <v-btn color="primary" :loading="saving" @click="submit">
        {{ t("Применить") }}
      </v-btn>
    </v-card-actions>
  </v-card>

  <v-dialog v-model="uploadDialog" max-width="720" persistent>
    <v-card>
      <v-card-title>{{ t("\u0417\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c \u0438\u0437\u043e\u0431\u0440\u0430\u0436\u0435\u043d\u0438\u0435") }}</v-card-title>
      <v-card-text>
        <file-upload-zone
          v-if="articleId"
          :file-id="`article-image-${articleId}`"
          :post-action="uploadUrl"
          accept="image/*,.svg"
          @close="handleUploadClose"
        />
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import type { VForm } from "vuetify/components";
import FileUploadZone, { type UploadCompleteEvent } from "@/components/FileUploadZone.vue";
import MarkdownTextEditor from "@/components/shared/MarkdownTextEditor.vue";
import { errorUtils } from "@/lib/errors";
import { ALL_ARTICLES_GROUPS, ARTICLE_SHARED_GROUP } from "@/services/article.service";
import {
  createAdminArticle,
  type ArticleFull,
  getAdminArticleImageUploadUrl,
  parseAdminArticleImageUploadResponse,
  updateAdminArticle,
} from "@/services/admin/admin-article.service";

type VFormInstance = InstanceType<typeof VForm>;

interface ArticleFormState {
  name: string;
  articleGroup: string;
  content: string;
  moderated: boolean;
  archived: boolean;
}

const props = defineProps<{
  input?: ArticleFull | null;
}>();

const emit = defineEmits<{
  (event: "close"): void;
  (event: "saved"): void;
}>();

const { t } = useI18n();

const formRef = ref<VFormInstance | null>(null);
const saving = ref(false);
const error = ref("");
const uploadDialog = ref(false);
const uploadError = ref("");

const form = reactive<ArticleFormState>({
  name: "",
  articleGroup: ARTICLE_SHARED_GROUP,
  content: "",
  moderated: false,
  archived: false,
});

const articleId = computed(() => props.input?.id ?? null);
const uploadUrl = computed(() =>
  articleId.value ? getAdminArticleImageUploadUrl(articleId.value) : "",
);

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
const nameRules = computed(() => [
  (value: unknown) =>
    (typeof value === "string" && value.length > 0 && value.length <= 255)
    || t("Обязательное поле. Не более N символов", { n: 255 }),
]);
const contentRules = computed(() => [
  (value: unknown) =>
    (typeof value === "string" && value.length > 0 && value.length <= 20000)
    || t("Обязательное поле. Не более N символов", { n: 20000 }),
]);
const groupRules = computed(() => [
  (value: unknown) => (typeof value === "string" && value.length > 0) || t("Обязательное поле"),
]);

watch(
  () => props.input,
  () => {
    form.name = props.input?.name ?? "";
    form.articleGroup = props.input?.articleGroup ?? ARTICLE_SHARED_GROUP;
    form.content = props.input?.content ?? "";
    form.moderated = props.input?.moderated ?? false;
    form.archived = props.input?.archived ?? false;
    error.value = "";
    uploadError.value = "";
    uploadDialog.value = false;
    formRef.value?.resetValidation();
  },
  { immediate: true },
);

async function submit(): Promise<void> {
  const validation = await formRef.value?.validate();
  if (validation && !validation.valid) {
    return;
  }

  saving.value = true;
  error.value = "";
  try {
    const payload = {
      name: form.name.trim(),
      articleGroup: form.articleGroup,
      moderated: form.moderated,
      archived: form.archived,
      content: form.content,
    };
    if (props.input?.id) {
      await updateAdminArticle(props.input.id, payload);
    } else {
      await createAdminArticle(payload);
    }
    emit("saved");
  } catch (submitError) {
    error.value = errorUtils.shortMessage(submitError);
  } finally {
    saving.value = false;
  }
}

function handleUploadClose(event: UploadCompleteEvent): void {
  uploadDialog.value = false;
  if (!event.uploaded) {
    return;
  }

  const imageUrl = parseAdminArticleImageUploadResponse(event.responseData);
  if (!imageUrl) {
    uploadError.value = t("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043e\u0431\u0440\u0430\u0431\u043e\u0442\u0430\u0442\u044c \u0438\u0437\u043e\u0431\u0440\u0430\u0436\u0435\u043d\u0438\u0435");
    return;
  }

  uploadError.value = "";
  insertUploadedImageMarkdown(imageUrl, event.filename);
}

function insertUploadedImageMarkdown(imageUrl: string, filename?: string): void {
  const alt = normalizeImageAlt(filename);
  const markdown = `![${alt}](${imageUrl})`;
  const content = form.content.trimEnd();
  form.content = content ? `${content}\n\n${markdown}` : markdown;
}

function normalizeImageAlt(filename?: string): string {
  if (!filename) {
    return "image";
  }

  const trimmed = filename.trim();
  if (!trimmed) {
    return "image";
  }

  const dotIndex = trimmed.lastIndexOf(".");
  return dotIndex > 0 ? trimmed.slice(0, dotIndex) : trimmed;
}
</script>
