<template>
  <div class="markdown-body" v-html="html"></div>
</template>

<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component'
import { Prop } from 'vue-property-decorator'
import { renderMarkdown } from '@/components/markdown'

@Component
export default class MarkdownTextRenderer extends Vue {
  @Prop({ type: String, default: '' }) readonly content!: string

  get html() {
    return renderMarkdown(this.content)
  }
}
</script>

<style scoped>
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
</style>

