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
  background: #ffffff;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  border-bottom: 1px solid #e5e7eb;
  background: #f9fafb;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-link {
  color: #4b5563;
  text-decoration: none;
  font-size: 14px;
}

.back-link:hover {
  color: #2563eb;
}

.title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.clear-btn {
  padding: 8px 16px;
  font-size: 14px;
  color: #374151;
  background: #ffffff;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.clear-btn:hover {
  background: #f3f4f6;
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-row {
  display: flex;
  width: 100%;
}

.message-row.user {
  justify-content: flex-end;
}

.message-row.ai {
  justify-content: flex-start;
}

.bubble {
  max-width: 75%;
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
  white-space: pre-wrap;
}

.message-row.user .bubble {
  background: #2563eb;
  color: #ffffff;
  border-bottom-right-radius: 4px;
}

.message-row.ai .bubble {
  background: #f3f4f6;
  color: #1f2937;
  border-bottom-left-radius: 4px;
}

.loading-bubble {
  display: flex;
  align-items: center;
  gap: 6px;
}

.dot {
  width: 8px;
  height: 8px;
  background: #9ca3af;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
}

.dot:nth-child(1) {
  animation-delay: -0.32s;
}

.dot:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes bounce {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

.error-tip {
  padding: 10px 24px;
  color: #b91c1c;
  background: #fee2e2;
  font-size: 14px;
  border-top: 1px solid #fecaca;
}

.input-area {
  display: flex;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #e5e7eb;
  background: #f9fafb;
}

.input-area input {
  flex: 1;
  padding: 12px 16px;
  font-size: 14px;
  color: #111827;
  background: #ffffff;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  outline: none;
  transition: border-color 0.2s;
}

.input-area input:focus {
  border-color: #2563eb;
}

.input-area input:disabled {
  background: #f3f4f6;
  cursor: not-allowed;
}

.input-area button {
  padding: 12px 28px;
  font-size: 14px;
  font-weight: 500;
  color: #ffffff;
  background: #2563eb;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s;
}

.input-area button:hover:not(:disabled) {
  background: #1d4ed8;
}

.input-area button:disabled {
  background: #93c5fd;
  cursor: not-allowed;
}
</style>
