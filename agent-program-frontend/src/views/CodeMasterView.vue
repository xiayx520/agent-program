<script setup lang="ts">
import { ref, onMounted } from 'vue'
import ChatRoom from '@/components/ChatRoom.vue'
import { generateChatId } from '@/utils/uuid'

const chatId = ref('')

onMounted(() => {
  chatId.value = generateChatId()
})

const buildUrl = (message: string) => {
  return `/api/ai/love_app/chat/emitter?message=${encodeURIComponent(message)}&chatId=${chatId.value}`
}
</script>

<template>
  <ChatRoom title="AI 编程大师" :build-url="buildUrl" />
</template>
