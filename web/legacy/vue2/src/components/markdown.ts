import MarkdownIt from 'markdown-it'
import DOMPurify from 'dompurify'

const md = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: true,
  typographer: true,
})

function renderToHtml(markdown: string): string {
  const raw = md.render(markdown || '')
  const wrapper = document.createElement('div')
  wrapper.innerHTML = raw
  // Ensure safe external links
  wrapper.querySelectorAll('a[href]').forEach((a) => {
    const el = a as HTMLAnchorElement
    el.target = '_blank'
    el.rel = 'noopener noreferrer'
  })
  return wrapper.innerHTML
}

export function renderMarkdown(markdown: string): string {
  return DOMPurify.sanitize(renderToHtml(markdown || ''))
}

export default md

