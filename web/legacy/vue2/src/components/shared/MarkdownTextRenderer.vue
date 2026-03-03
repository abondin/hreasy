<template>
  <div class="markdown-wrapper">
    <div class="markdown-body" v-html="html"></div>
    <div v-if="isHtml" class="legacy-note">{{ $t('Режим совместимости HTML') }}</div>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component'
import { Prop } from 'vue-property-decorator'
import { renderMarkdown } from '@/components/markdown'
import DOMPurify from 'dompurify'

@Component
export default class MarkdownTextRenderer extends Vue {
  @Prop({ type: String, default: '' }) readonly content!: string

  get html() {
    const src = this.content || ''
    if (this.isHtml) {
      return DOMPurify.sanitize(src)
    }
    return renderMarkdown(src)
  }

  get isHtml() {
    const src = this.content || ''
    // Heuristic: if looks like HTML, sanitize as-is for backward compatibility
    return /<\s*\w+[^>]*>/i.test(src)
  }
}
</script>

<style scoped>
.markdown-wrapper {
  position: relative;
}
.markdown-body {
  line-height: 1.5;
  word-wrap: break-word;
}
.markdown-body h1, .markdown-body h2, .markdown-body h3 {
  margin: 0.6rem 0 0.4rem;
  font-weight: 600;
}
.markdown-body p { margin: 0.4rem 0; }
.markdown-body ul, .markdown-body ol { padding-left: 1.2rem; }
.markdown-body code {
  background: rgba(0,0,0,0.06);
  padding: 0.1rem 0.3rem;
  border-radius: 4px;
}
.markdown-body pre {
  background: rgba(0,0,0,0.06);
  padding: 0.6rem;
  border-radius: 6px;
  overflow: auto;
}
.markdown-body a { color: var(--v-primary-base); text-decoration: underline; }

.legacy-note {
  position: absolute;
  right: 8px;
  bottom: 4px;
  color: rgba(0, 0, 0, 0.45);
  font-size: 10px;
}
</style>
