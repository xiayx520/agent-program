<script setup lang="ts">
import { ref, computed, nextTick, onUnmounted } from 'vue'
import { createSSE } from '@/api/sse'

interface ChatMessage {
  role: 'user' | 'ai'
  content: string
}

interface Props {
  title: string
  buildUrl: (message: string) => string
}

const props = defineProps<Props>()

const input = ref('')
const messages = ref<ChatMessage[]>([])
const loading = ref(false)
const error = ref('')
const currentAiContent = ref('')
const messagesRef = ref<HTMLElement | null>(null)

let sseController: { close: () => void } | null = null

const canSend = computed(() => input.value.trim().length > 0 && !loading.value)

const scrollToBottom = async () => {
  await nextTick()
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

const finishAiMessage = () => {
  if (currentAiContent.value) {
    messages.value.push({ role: 'ai', content: currentAiContent.value })
    currentAiContent.value = ''
  }
}

const sendMessage = () => {
  const text = input.value.trim()
  if (!text) {
    error.value = '请输入消息内容'
    return
  }

  error.value = ''
  messages.value.push({ role: 'user', content: text })
  input.value = ''
  loading.value = true
  currentAiContent.value = ''
  scrollToBottom()

  const url = props.buildUrl(text)

  sseController = createSSE({
    url,
    onMessage: (data) => {
      currentAiContent.value += data
      scrollToBottom()
    },
    onError: (err) => {
      loading.value = false
      finishAiMessage()
      error.value = err.message || '请求失败，请稍后重试'
      scrollToBottom()
    },
    onComplete: () => {
      loading.value = false
      finishAiMessage()
      scrollToBottom()
    }
  })
}

const clearChat = () => {
  sseController?.close()
  sseController = null
  messages.value = []
  currentAiContent.value = ''
  input.value = ''
  error.value = ''
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
        <div class="bubble">{{ msg.content }}</div>
      </div>

      <div v-if="currentAiContent" class="message-row ai">
        <div class="bubble">{{ currentAiContent }}</div>
      </div>

      <div v-if="loading && !currentAiContent" class="message-row ai">
        <div class="bubble loading-bubble">
          <span class="dot" />
          <span class="dot" />
          <span class="dot" />
        </div>
      </div>
    </main>

    <div v-if="error" class="error-tip">{{ error }}</div>

    <footer class="input-area">
      <input
        v-model="input"
        type="text"
        placeholder="请输入消息，按 Enter 发送"
        :disabled="loading"
        @keyup.enter="sendMessage"
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
  animation: fadeIn 0.35s ease-out both;
}

.message-row.user {
  justify-content: flex-end;
}

.message-row.ai {
  justify-content: flex-start;
}

.bubble {
  max-width: min(78%, 760px);
  padding: 14px 18px;
  border-radius: var(--radius-md);
  font-size: 15px;
  line-height: 1.7;
  word-break: break-word;
  white-space: pre-wrap;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.22);
}

.message-row.user .bubble {
  background: linear-gradient(135deg, var(--tech-primary), var(--tech-primary-deep));
  color: #ffffff;
  border-bottom-right-radius: 4px;
  box-shadow: 0 6px 26px rgba(42, 111, 255, 0.35);
}

.message-row.ai .bubble {
  background: var(--tech-panel);
  color: var(--tech-text);
  border: 1px solid var(--tech-border);
  border-bottom-left-radius: 4px;
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
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
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid var(--tech-border);
  background: rgba(10, 20, 37, 0.88);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
}

.input-area input {
  flex: 1;
  padding: 14px 18px;
  font-size: 15px;
  color: var(--tech-text);
  background: rgba(5, 11, 20, 0.55);
  border: 1px solid var(--tech-border);
  border-radius: var(--radius-md);
  outline: none;
  transition:
    border-color 0.2s,
    box-shadow 0.2s;
}

.input-area input::placeholder {
  color: var(--tech-text-muted);
}

.input-area input:focus {
  border-color: var(--tech-accent);
  box-shadow:
    0 0 0 3px rgba(0, 240, 255, 0.1),
    0 0 20px rgba(0, 240, 255, 0.12);
}

.input-area input:disabled {
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

  .bubble {
    max-width: 86%;
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
