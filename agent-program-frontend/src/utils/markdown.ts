import { marked } from 'marked'
import DOMPurify from 'dompurify'
import hljs from 'highlight.js/lib/core'

import bash from 'highlight.js/lib/languages/bash'
import c from 'highlight.js/lib/languages/c'
import cpp from 'highlight.js/lib/languages/cpp'
import css from 'highlight.js/lib/languages/css'
import dockerfile from 'highlight.js/lib/languages/dockerfile'
import go from 'highlight.js/lib/languages/go'
import html from 'highlight.js/lib/languages/xml'
import ini from 'highlight.js/lib/languages/ini'
import java from 'highlight.js/lib/languages/java'
import javascript from 'highlight.js/lib/languages/javascript'
import json from 'highlight.js/lib/languages/json'
import nginx from 'highlight.js/lib/languages/nginx'
import plaintext from 'highlight.js/lib/languages/plaintext'
import powershell from 'highlight.js/lib/languages/powershell'
import properties from 'highlight.js/lib/languages/properties'
import python from 'highlight.js/lib/languages/python'
import rust from 'highlight.js/lib/languages/rust'
import shell from 'highlight.js/lib/languages/shell'
import sql from 'highlight.js/lib/languages/sql'
import typescript from 'highlight.js/lib/languages/typescript'
import xml from 'highlight.js/lib/languages/xml'
import yaml from 'highlight.js/lib/languages/yaml'

const languages: Record<string, unknown> = {
  bash,
  c,
  cpp,
  css,
  dockerfile,
  go,
  html,
  ini,
  java,
  javascript,
  json,
  nginx,
  plaintext,
  powershell,
  properties,
  python,
  rust,
  shell,
  sql,
  typescript,
  xml,
  yaml
}

Object.entries(languages).forEach(([name, define]) => {
  hljs.registerLanguage(name, define as typeof bash)
})

const copyIconSvg =
  '<svg class="icon-copy" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="9" y="9" width="13" height="13" rx="2" ry="2" /><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" /></svg>'

const copyBtnHtml =
  `<button class="code-copy-btn" type="button" title="复制" aria-label="复制代码">${copyIconSvg}</button>`

const escapeHtml = (text: string): string =>
  text.replace(/[&<>"']/g, (char) => {
    const entities: Record<string, string> = {
      '&': '&amp;',
      '<': '&lt;',
      '>': '&gt;',
      '"': '&quot;',
      "'": '&#39;'
    }
    return entities[char] ?? char
  })

const slugify = (text: string): string => {
  return text
    .replace(/<[^>]+>/g, '')
    .trim()
    .toLowerCase()
    .replace(/[^\w\u4e00-\u9fa5]+/g, '-')
    .replace(/^-+|-+$/g, '')
    || 'heading'
}

marked.use({
  gfm: true,
  breaks: false,
  renderer: {
    heading({ text, depth }) {
      const id = slugify(text)
      return `<h${depth} id="${id}" class="md-heading">${text}</h${depth}>`
    },
    code({ text, lang }) {
      const langName = lang?.trim().split(/\s+/)[0]?.toLowerCase() || ''
      const language = langName && hljs.getLanguage(langName) ? langName : 'plaintext'
      let highlighted: string
      try {
        highlighted = hljs.highlight(text, { language, ignoreIllegals: true }).value
      } catch {
        highlighted = escapeHtml(text)
      }
      const langLabel = language === 'plaintext' ? 'TEXT' : language.toUpperCase()
      return `<div class="code-block-wrapper"><div class="code-header"><span class="code-lang">${langLabel}</span>${copyBtnHtml}</div><pre><code class="hljs language-${language}">${highlighted}</code></pre></div>`
    },
    codespan({ text }) {
      return `<code class="inline-code">${text}</code>`
    }
  }
})

const normalizeMarkdown = (raw: string): string => {
  return raw
    .replace(/\r\n/g, '\n')
    .replace(/\r/g, '\n')
    .replace(/[\u00A0\u1680\u2000-\u200A\u202F\u205F\u3000]/g, ' ')
    .replace(/^(#{1,6})([^ \n])/gm, '$1 $2')
    .trim()
}

export function renderMarkdown(raw: string): string {
  if (!raw) return ''
  const normalized = normalizeMarkdown(raw)
  const html = marked.parse(normalized, { async: false }) as string
  return DOMPurify.sanitize(html)
}
