<script setup lang="ts">
import { ref, computed, nextTick, onUnmounted } from 'vue'
import { createSSE } from '@/api/sse'
import { renderMarkdown } from '@/utils/markdown'
import AiAvatar from '@/components/AiAvatar.vue'

const checkIconSvg =
  '<svg class="icon-copy" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12" /></svg>'

interface ChatMessage {
  role: 'user' | 'ai'
  content: string
  timestamp: number
}

interface Props {
  title: string
  buildUrl: (message: string) => string
  /** 每收到一个 SSE 块后追加的分隔符，例如 AI 超级智能体可设置为 '\n' 实现逐段换行 */
  chunkSeparator?: string
  /** SSE 整体超时时间，单位毫秒，默认 180000（3 分钟） */
  timeout?: number
}

const props = defineProps<Props>()

const input = ref('')
const messages = ref<ChatMessage[]>([])
const loading = ref(false)
const error = ref('')
const currentAiContent = ref('')
const aiTimestamp = ref<number | null>(null)
const messagesRef = ref<HTMLElement | null>(null)
const copiedId = ref<string | null>(null)
const textareaRef = ref<HTMLTextAreaElement | null>(null)

let sseController: { close: () => void } | null = null

const canSend = computed(() => input.value.trim().length > 0 && !loading.value)

const autoResize = () => {
  const el = textareaRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = `${Math.min(el.scrollHeight, 160)}px`
}

const handleEnter = (e: KeyboardEvent) => {
  if (e.shiftKey || e.isComposing || !canSend.value) return
  e.preventDefault()
  sendMessage()
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

const formatTime = (timestamp: number) => {
  return new Date(timestamp).toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

const copyText = async (text: string, id: string) => {
  try {
    await navigator.clipboard.writeText(text)
  } catch {
    const textarea = document.createElement('textarea')
    textarea.value = text
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
  }
  copiedId.value = id
  setTimeout(() => {
    copiedId.value = null
  }, 1500)
}

const handleBubbleClick = (e: MouseEvent) => {
  const btn = (e.target as HTMLElement).closest('.code-copy-btn')
  if (!btn) return
  const wrapper = btn.closest('.code-block-wrapper')
  const codeEl = wrapper?.querySelector('code')
  if (!codeEl) return
  const text = codeEl.textContent || ''
  const id = `code-${Date.now()}`
  copyText(text, id)
  const icon = btn.querySelector('svg')
  const originalTitle = btn.getAttribute('title')
  if (icon) {
    icon.innerHTML = '<polyline points="20 6 9 17 4 12" />'
  }
  btn.setAttribute('title', '已复制')
  btn.classList.add('copied')
  setTimeout(() => {
    if (icon) {
      icon.innerHTML = '<rect x="9" y="9" width="13" height="13" rx="2" ry="2" /><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />'
    }
    btn.setAttribute('title', originalTitle || '复制')
    btn.classList.remove('copied')
  }, 1500)
}

const finishAiMessage = () => {
  if (currentAiContent.value) {
    messages.value.push({
      role: 'ai',
      content: currentAiContent.value,
      timestamp: aiTimestamp.value || Date.now()
    })
    currentAiContent.value = ''
    aiTimestamp.value = null
  }
}

const sendMessage = () => {
  if (loading.value) return

  const text = input.value.trim()
  if (!text) {
    error.value = '请输入消息内容'
    return
  }

  sseController?.close()
  sseController = null

  error.value = ''
  messages.value.push({ role: 'user', content: text, timestamp: Date.now() })
  input.value = ''
  nextTick(autoResize)
  loading.value = true
  currentAiContent.value = ''
  aiTimestamp.value = null
  scrollToBottom()

  const url = props.buildUrl(text)

  sseController = createSSE({
    url,
    timeout: props.timeout,
    onMessage: (data) => {
      if (aiTimestamp.value === null) {
        aiTimestamp.value = Date.now()
      }
      if (props.chunkSeparator && currentAiContent.value) {
        currentAiContent.value += props.chunkSeparator
      }
      currentAiContent.value += data
      scrollToBottom()
    },
    onError: (err) => {
      loading.value = false
      finishAiMessage()
      error.value = err.message || '服务繁忙，请稍后重试'
      scrollToBottom()
    },
    onComplete: () => {
      loading.value = false
      const hadContent = currentAiContent.value.trim().length > 0
      finishAiMessage()
      // 若连接正常结束但没有任何有效内容返回，则给出友好提示
      if (!hadContent) {
        const lastMsg = messages.value[messages.value.length - 1]
        if (!lastMsg || lastMsg.role !== 'ai') {
          error.value = '未收到有效回复，请稍后重试'
        }
      }
      scrollToBottom()
    }
  })
}

const clearChat = () => {
  sseController?.close()
  sseController = null
  messages.value = []
  currentAiContent.value = ''
  aiTimestamp.value = null
  input.value = ''
  error.value = ''
  nextTick(autoResize)
}

onUnmounted(() => {
  sseController?.close()
})
</script>

<template>
  <div class="chat-room">
    <header class="chat-header">
      <div class="header-left">
        <router-link to="/" class="back-link">← 返回</router-link>
        <h2 class="title">{{ title }}</h2>
      </div>
      <button class="clear-btn" @click="clearChat">清空会话</button>
    </header>

    <main ref="messagesRef" class="messages">
      <div
        v-for="(msg, index) in messages"
        :key="index"
        :class="['message-row', msg.role]"
      >
        <div v-if="msg.role === 'ai'" class="ai-profile">
          <AiAvatar class="avatar" />
          <span class="ai-name">{{ title }}</span>
        </div>
        <div class="bubble-wrap">
          <div
            class="bubble markdown-body"
            v-html="msg.role === 'ai' ? renderMarkdown(msg.content) : msg.content"
            @click="msg.role === 'ai' ? handleBubbleClick($event) : undefined"
          ></div>
          <div class="message-actions">
            <span class="msg-time">{{ formatTime(msg.timestamp) }}</span>
            <button
              class="copy-btn"
              @click="copyText(msg.content, `msg-${index}`)"
            >
              <svg class="icon-copy" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="9" y="9" width="13" height="13" rx="2" ry="2" />
                <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
              </svg>
              {{ copiedId === `msg-${index}` ? '已复制' : '复制' }}
            </button>
          </div>
        </div>
      </div>

      <div v-if="currentAiContent" class="message-row ai">
        <div class="ai-profile">
          <AiAvatar class="avatar" />
          <span class="ai-name">{{ title }}</span>
        </div>
        <div class="bubble-wrap">
          <div
            class="bubble markdown-body"
            v-html="renderMarkdown(currentAiContent)"
            @click="handleBubbleClick"
          ></div>
          <div class="message-actions">
            <span class="msg-time">{{ formatTime(aiTimestamp || Date.now()) }}</span>
            <button
              class="copy-btn"
              @click="copyText(currentAiContent, 'ai-stream')"
            >
              <svg class="icon-copy" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="9" y="9" width="13" height="13" rx="2" ry="2" />
                <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
              </svg>
              {{ copiedId === 'ai-stream' ? '已复制' : '复制' }}
            </button>
          </div>
        </div>
      </div>

      <div v-if="loading && !currentAiContent" class="message-row ai">
        <div class="ai-profile">
          <AiAvatar class="avatar" />
          <span class="ai-name">{{ title }}</span>
        </div>
        <div class="bubble loading-bubble">
          <span class="dot" />
          <span class="dot" />
          <span class="dot" />
        </div>
      </div>
    </main>

    <div v-if="error" class="error-tip">{{ error }}</div>

    <footer class="input-area">
      <textarea
        ref="textareaRef"
        v-model="input"
        placeholder="请输入消息，按 Enter 发送，Shift+Enter 换行"
        :disabled="loading"
        rows="1"
        @input="autoResize"
        @keydown.enter="handleEnter"
      />
      <button :disabled="!canSend" @click="sendMessage">
        {{ loading ? '发送中' : '发送' }}
      </button>
    </footer>
  </div>
</template>

<style scoped>
.chat-room {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background:
    radial-gradient(ellipse 80% 40% at 50% -5%, rgba(42, 111, 255, 0.15), transparent),
    var(--color-background);
  position: relative;
  overflow: hidden;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 24px;
  border-bottom: 1px solid var(--tech-border);
  background: rgba(10, 20, 37, 0.8);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--tech-text-muted);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition:
    color 0.2s,
    text-shadow 0.2s;
}

.back-link:hover {
  color: var(--tech-accent);
  text-shadow: 0 0 10px rgba(0, 240, 255, 0.4);
}

.title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--color-heading);
  letter-spacing: 0.02em;
}

.clear-btn {
  padding: 8px 16px;
  font-size: 13px;
  font-weight: 600;
  color: var(--tech-text);
  background: transparent;
  border: 1px solid var(--tech-border);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.2s;
}

.clear-btn:hover {
  border-color: var(--tech-accent);
  color: var(--tech-accent);
  background: var(--tech-accent-soft);
  box-shadow: 0 0 16px rgba(0, 240, 255, 0.15);
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.message-row {
  display: flex;
  width: 100%;
}

.message-row.user {
  justify-content: flex-end;
}

.message-row.ai {
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}

.ai-profile {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 2px;
}

.ai-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-heading);
}

.bubble-wrap {
  display: flex;
  flex-direction: column;
  max-width: min(78%, 760px);
}

.message-row.user .bubble-wrap {
  align-items: flex-end;
}

.message-row.ai .bubble-wrap {
  align-items: flex-start;
}

.bubble {
  padding: 14px 18px;
  border-radius: var(--radius-md);
  font-size: 15px;
  line-height: 1.7;
  word-break: break-word;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.22);
}

.message-row.user .bubble {
  background: linear-gradient(135deg, var(--tech-primary), var(--tech-primary-deep));
  color: #ffffff;
  border-bottom-right-radius: 4px;
  box-shadow: 0 6px 26px rgba(42, 111, 255, 0.35);
  white-space: pre-wrap;
}

.message-row.ai .bubble {
  background: var(--tech-panel);
  color: var(--tech-text);
  border: 1px solid var(--tech-border);
  border-bottom-left-radius: 4px;
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
}

.markdown-body {
  white-space: pre-wrap;
}

.markdown-body :deep(:first-child) {
  margin-top: 0;
}

.markdown-body :deep(:last-child) {
  margin-bottom: 0;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3),
.markdown-body :deep(h4),
.markdown-body :deep(h5),
.markdown-body :deep(h6) {
  margin: 18px 0 10px;
  font-weight: 700;
  color: var(--tech-heading);
  line-height: 1.35;
}

.markdown-body :deep(h1) {
  font-size: 20px;
  border-bottom: 1px solid var(--tech-border);
  padding-bottom: 8px;
}

.markdown-body :deep(h2) {
  font-size: 18px;
}

.markdown-body :deep(h3) {
  font-size: 16px;
}

.markdown-body :deep(p) {
  margin: 10px 0;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  margin: 10px 0;
  padding-left: 22px;
}

.markdown-body :deep(li) {
  margin: 4px 0;
}

.markdown-body :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 14px 0;
  font-size: 14px;
  border: 1px solid var(--tech-border);
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.markdown-body :deep(th),
.markdown-body :deep(td) {
  padding: 10px 12px;
  border: 1px solid var(--tech-border);
  text-align: left;
  vertical-align: top;
}

.markdown-body :deep(th) {
  background: var(--tech-bg-mute);
  color: var(--tech-heading);
  font-weight: 600;
}

.markdown-body :deep(tr:nth-child(even)) {
  background: rgba(138, 174, 255, 0.04);
}

.markdown-body :deep(tr:hover) {
  background: rgba(0, 240, 255, 0.06);
}

.markdown-body :deep(blockquote) {
  margin: 14px 0;
  padding: 10px 16px;
  border-left: 3px solid var(--tech-accent);
  background: var(--tech-accent-soft);
  border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
  color: var(--tech-text-muted);
}

.markdown-body :deep(hr) {
  border: none;
  border-top: 1px solid var(--tech-border);
  margin: 16px 0;
}

.markdown-body :deep(a) {
  color: var(--tech-accent);
  text-decoration: none;
}

.markdown-body :deep(a:hover) {
  text-decoration: underline;
}

.markdown-body :deep(strong) {
  color: var(--tech-heading);
  font-weight: 700;
}

.markdown-body :deep(em) {
  color: var(--tech-text);
  font-style: italic;
}

.markdown-body :deep(del) {
  color: var(--tech-text-muted);
  text-decoration: line-through;
}

.markdown-body :deep(img) {
  max-width: 100%;
  border-radius: var(--radius-sm);
}

.markdown-body :deep(input[type='checkbox']) {
  margin-right: 8px;
  accent-color: var(--tech-accent);
}

.markdown-body :deep(.inline-code) {
  display: inline;
  padding: 2px 6px;
  background: rgba(0, 240, 255, 0.1);
  border: 1px solid rgba(0, 240, 255, 0.18);
  border-radius: 6px;
  color: var(--tech-accent);
  font-family:
    'Fira Code',
    'SF Mono',
    Monaco,
    Consolas,
    monospace;
  font-size: 0.9em;
  word-break: break-word;
}

.markdown-body :deep(.code-block-wrapper) {
  position: relative;
  margin: 14px 0;
  border: 1px solid var(--tech-border);
  border-radius: var(--radius-md);
  background: rgba(5, 11, 20, 0.75);
  overflow: hidden;
}

.markdown-body :deep(.code-block-wrapper pre) {
  margin: 0;
  padding: 14px 16px;
  overflow-x: auto;
  line-height: 1.6;
}

.markdown-body :deep(.code-block-wrapper code) {
  font-family:
    'Fira Code',
    'SF Mono',
    Monaco,
    Consolas,
    monospace;
  font-size: 13px;
  background: transparent !important;
  padding: 0 !important;
  border: none !important;
  color: inherit;
}

.markdown-body :deep(pre),
.markdown-body :deep(pre code),
.markdown-body :deep(pre code *) {
  white-space: pre !important;
}

.markdown-body :deep(.code-header) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 6px 12px;
  background: rgba(0, 0, 0, 0.25);
  border-bottom: 1px solid var(--tech-border);
}

.markdown-body :deep(.code-lang) {
  font-family:
    'Fira Code',
    'SF Mono',
    Monaco,
    Consolas,
    monospace;
  font-size: 11px;
  font-weight: 600;
  color: var(--tech-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.markdown-body :deep(.code-copy-btn) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  padding: 0;
  color: var(--tech-text-muted);
  background: transparent;
  border: 1px solid transparent;
  border-radius: 6px;
  cursor: pointer;
  transition:
    color 0.2s,
    background 0.2s,
    border-color 0.2s,
    box-shadow 0.2s,
    transform 0.15s;
}

.markdown-body :deep(.code-copy-btn:hover) {
  color: var(--tech-accent);
  background: rgba(0, 240, 255, 0.1);
  border-color: rgba(0, 240, 255, 0.35);
  box-shadow: 0 0 12px rgba(0, 240, 255, 0.16);
}

.markdown-body :deep(.code-copy-btn:active) {
  transform: scale(0.92);
}

.markdown-body :deep(.code-copy-btn:focus-visible) {
  outline: 2px solid rgba(0, 240, 255, 0.7);
  outline-offset: 2px;
}

.markdown-body :deep(.code-copy-btn.copied) {
  color: var(--tech-success);
  background: rgba(0, 229, 168, 0.12);
  border-color: rgba(0, 229, 168, 0.4);
}

.markdown-body :deep(.code-copy-btn .icon-copy) {
  width: 14px;
  height: 14px;
  flex: none;
}

.message-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 6px;
  opacity: 0;
  transition: opacity 0.2s;
}

.message-row:hover .message-actions {
  opacity: 1;
}

.msg-time {
  font-size: 12px;
  color: var(--tech-text-muted);
}

.copy-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  font-size: 12px;
  color: var(--tech-text-muted);
  background: rgba(13, 25, 44, 0.6);
  border: 1px solid var(--tech-border);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition:
    color 0.2s,
    background 0.2s,
    border-color 0.2s;
}

.copy-btn:hover {
  color: var(--tech-accent);
  background: var(--tech-accent-soft);
  border-color: var(--tech-border-hover);
}

.icon-copy {
  width: 13px;
  height: 13px;
}

.loading-bubble {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-width: 68px;
  min-height: 48px;
  background: var(--tech-panel) !important;
  border: 1px solid var(--tech-border);
}

.dot {
  width: 8px;
  height: 8px;
  background: var(--tech-accent);
  border-radius: 50%;
  box-shadow: 0 0 8px var(--tech-accent);
  animation: bounce 1.4s infinite ease-in-out both;
}

.dot:nth-child(1) {
  animation-delay: -0.32s;
}

.dot:nth-child(2) {
  animation-delay: -0.16s;
}

@media (hover: none) {
  .message-actions {
    opacity: 1;
  }
}

.error-tip {
  padding: 12px 24px;
  color: var(--tech-danger);
  background: var(--tech-danger-soft);
  border-top: 1px solid rgba(255, 77, 109, 0.25);
  font-size: 14px;
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}

.input-area {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid var(--tech-border);
  background: rgba(10, 20, 37, 0.88);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
}

.input-area textarea {
  flex: 1;
  min-height: 50px;
  max-height: 160px;
  padding: 14px 18px;
  font-size: 15px;
  line-height: 1.6;
  color: var(--tech-text);
  background: rgba(5, 11, 20, 0.55);
  border: 1px solid var(--tech-border);
  border-radius: var(--radius-md);
  outline: none;
  resize: none;
  overflow-y: auto;
  transition:
    border-color 0.2s,
    box-shadow 0.2s;
}

.input-area textarea::placeholder {
  color: var(--tech-text-muted);
}

.input-area textarea:focus {
  border-color: var(--tech-accent);
  box-shadow:
    0 0 0 3px rgba(0, 240, 255, 0.1),
    0 0 20px rgba(0, 240, 255, 0.12);
}

.input-area textarea:disabled {
  background: rgba(255, 255, 255, 0.04);
  cursor: not-allowed;
  color: var(--tech-text-muted);
}

.input-area button {
  padding: 14px 32px;
  font-size: 15px;
  font-weight: 700;
  color: var(--tech-bg);
  background: linear-gradient(135deg, var(--tech-accent), var(--tech-primary));
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition:
    transform 0.2s,
    box-shadow 0.2s,
    filter 0.2s;
  box-shadow: 0 4px 22px rgba(42, 111, 255, 0.35);
}

.input-area button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0, 240, 255, 0.35);
  filter: brightness(1.08);
}

.input-area button:disabled {
  background: rgba(255, 255, 255, 0.12);
  color: var(--tech-text-muted);
  cursor: not-allowed;
  box-shadow: none;
}

@media (max-width: 640px) {
  .chat-header {
    padding: 12px 16px;
  }

  .title {
    font-size: 16px;
  }

  .back-link {
    font-size: 13px;
  }

  .messages {
    padding: 16px;
    gap: 14px;
  }

  .bubble-wrap {
    max-width: 86%;
  }

  .bubble {
    padding: 12px 14px;
    font-size: 14px;
  }

  .input-area {
    padding: 12px 16px;
  }

  .input-area button {
    padding: 12px 20px;
  }
}
</style>
