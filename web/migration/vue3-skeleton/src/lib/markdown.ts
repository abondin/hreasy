import MarkdownIt from "markdown-it";
import type { PluginSimple } from "markdown-it";
import * as markdownItEmoji from "markdown-it-emoji";

const markdown = new MarkdownIt({
  html: false,
  linkify: true,
  typographer: true,
});

const emojiModule = markdownItEmoji as unknown as {
  default?: PluginSimple;
  bare?: PluginSimple;
};

const emojiPlugin = emojiModule.default ?? emojiModule.bare;

if (emojiPlugin) {
  markdown.use(emojiPlugin);
}

export function renderMarkdown(value: string): string {
  return markdown.render(value);
}
