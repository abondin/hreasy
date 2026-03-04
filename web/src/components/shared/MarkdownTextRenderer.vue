<!--
  Renders Markdown content with legacy HTML compatibility.
  Ported from the Vue 2 implementation.
  -->
<template>
  <div class="markdown-wrapper">
    <div class="markdown-body" v-html="computedHtml" />
    <div v-if="isHtml" class="legacy-note">
      {{ t("Режим совместимости HTML") }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import DOMPurify from "dompurify";
import { renderMarkdown } from "@/lib/markdown";

const props = defineProps<{
  content?: string;
}>();

const { t } = useI18n();

const normalizedContent = computed(() => props.content ?? "");

const isHtml = computed(() => /<\s*\w+[^>]*>/i.test(normalizedContent.value));

const computedHtml = computed(() => {
  if (isHtml.value) {
    return DOMPurify.sanitize(normalizedContent.value);
  }
  return renderMarkdown(normalizedContent.value);
});
</script>

<style scoped>
.markdown-wrapper {
  position: relative;
}

.markdown-body {
  line-height: 1.5;
  word-wrap: break-word;
}

.markdown-body h1,
.markdown-body h2,
.markdown-body h3 {
  margin: 0.6rem 0 0.4rem;
  font-weight: 600;
}

.markdown-body p {
  margin: 0.4rem 0;
}

.markdown-body ul,
.markdown-body ol {
  padding-left: 1.2rem;
}

.markdown-body code {
  background: rgba(0, 0, 0, 0.06);
  padding: 0.1rem 0.3rem;
  border-radius: 4px;
}

.markdown-body pre {
  background: rgba(0, 0, 0, 0.06);
  padding: 0.6rem;
  border-radius: 6px;
  overflow: auto;
}

.markdown-body a {
  color: var(--v-theme-primary);
  text-decoration: underline;
}

.legacy-note {
  position: absolute;
  right: 8px;
  bottom: 4px;
  color: rgba(0, 0, 0, 0.45);
  font-size: 10px;
}
</style>
