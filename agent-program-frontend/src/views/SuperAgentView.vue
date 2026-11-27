<script setup lang="ts">
import ChatRoom from '@/components/ChatRoom.vue'

const buildUrl = (message: string) => {
  return `/api/ai/chat/manus?message=${encodeURIComponent(message)}`
}
</script>

<template>
  <ChatRoom
    title="AI 超级智能体"
    :build-url="buildUrl"
    :chunk-separator="'\n'"
    :timeout="300000"
  />
</template>
