<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import TechBackground from '@/components/TechBackground.vue'

const fullTitle = '智能应用中心'
const displayedTitle = ref('')
const typingDone = ref(false)

let typeTimer: number | null = null
let charIndex = 0

onMounted(() => {
  displayedTitle.value = ''
  typingDone.value = false
  charIndex = 0

  typeTimer = window.setInterval(() => {
    if (charIndex >= fullTitle.length) {
      if (typeTimer !== null) {
        clearInterval(typeTimer)
        typeTimer = null
      }
      typingDone.value = true
      return
    }
    displayedTitle.value += fullTitle[charIndex]
    charIndex++
  }, 140)
})

onUnmounted(() => {
  if (typeTimer !== null) {
    clearInterval(typeTimer)
  }
})
</script>

<template>
  <div class="home">
    <TechBackground class="bg-canvas" />
    <h1 class="home-title" :aria-label="fullTitle">
      {{ displayedTitle }}<span v-if="!typingDone" class="cursor"></span>
    </h1>
    <p v-if="typingDone" class="home-tagline">探索AI的无限可能</p>
    <p v-if="typingDone" class="home-subtitle">选择下方应用开始体验</p>
    <div class="apps">
      <router-link to="/code-master" class="app-card">
        <div class="icon">💻</div>
        <h2>AI 编程大师</h2>
        <p>专业的 AI 编程助手，实时解答代码问题</p>
      </router-link>
      <router-link to="/super-agent" class="app-card">
        <div class="icon">🤖</div>
        <h2>AI 超级智能体</h2>
        <p>全能 AI 智能体，处理各类复杂任务</p>
      </router-link>
    </div>
  </div>
</template>

<style scoped>
.home {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: clamp(24px, 6vw, 64px);
  text-align: center;
  background:
    radial-gradient(circle at 20% 30%, rgba(0, 240, 255, 0.08), transparent 28%),
    radial-gradient(circle at 80% 70%, rgba(42, 111, 255, 0.1), transparent 32%),
    var(--color-background);
  position: relative;
  overflow: hidden;
}

.home::before,
.home::after {
  content: '';
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  pointer-events: none;
}

.home::before {
  width: 360px;
  height: 360px;
  top: -120px;
  right: -120px;
  background: rgba(42, 111, 255, 0.2);
  animation: float 8s ease-in-out infinite;
}

.home::after {
  width: 280px;
  height: 280px;
  bottom: -80px;
  left: -80px;
  background: rgba(0, 240, 255, 0.12);
  animation: float 10s ease-in-out infinite reverse;
}

.home > *:not(.bg-canvas) {
  position: relative;
  z-index: 1;
}

.home-title {
  margin: 0;
  font-size: clamp(32px, 6vw, 52px);
  font-weight: 800;
  letter-spacing: -0.02em;
  background: linear-gradient(135deg, #ffffff 20%, var(--tech-accent) 55%, var(--tech-primary) 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 0 40px rgba(0, 240, 255, 0.18);
}

.cursor {
  display: inline-block;
  width: 3px;
  height: 0.9em;
  margin-left: 6px;
  vertical-align: middle;
  background: var(--tech-accent);
  border-radius: 2px;
  box-shadow: 0 0 10px var(--tech-accent);
  -webkit-text-fill-color: initial;
  animation: blink 1s step-end infinite;
}

@keyframes blink {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0;
  }
}

.home-tagline {
  margin-top: 12px;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.12em;
  color: var(--tech-accent);
  text-transform: uppercase;
  text-shadow: 0 0 12px rgba(0, 240, 255, 0.35);
  animation: fadeIn 0.6s ease-out both;
}

.home-subtitle {
  margin-top: 10px;
  font-size: clamp(15px, 2vw, 18px);
  color: var(--tech-text-muted);
  max-width: 520px;
  line-height: 1.7;
  animation: fadeIn 0.6s ease-out 0.1s both;
}

.apps {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: clamp(20px, 3vw, 36px);
  margin-top: clamp(40px, 6vw, 64px);
  width: 100%;
  max-width: 820px;
}

.app-card {
  flex: 1 1 260px;
  max-width: 360px;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: clamp(32px, 4vw, 44px) clamp(24px, 3vw, 36px);
  color: var(--color-text);
  text-decoration: none;
  background: var(--tech-panel);
  border: 1px solid var(--tech-border);
  border-radius: var(--radius-lg);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  box-shadow: 0 10px 40px var(--tech-shadow);
  transition:
    transform 0.35s cubic-bezier(0.34, 1.56, 0.64, 1),
    border-color 0.3s,
    box-shadow 0.3s;
  position: relative;
  overflow: hidden;
  animation: fadeIn 0.6s ease-out both;
}

.app-card:nth-child(1) {
  animation-delay: 0.15s;
}

.app-card:nth-child(2) {
  animation-delay: 0.3s;
}

.app-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(0, 240, 255, 0.08), transparent 55%);
  opacity: 0;
  transition: opacity 0.3s;
  pointer-events: none;
}

.app-card:hover {
  transform: translateY(-8px) scale(1.02);
  border-color: var(--tech-border-hover);
  box-shadow: var(--tech-glow), 0 24px 60px var(--tech-shadow);
}

.app-card:hover::before {
  opacity: 1;
}

.icon {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 38px;
  margin-bottom: 24px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(42, 111, 255, 0.25), rgba(0, 240, 255, 0.12));
  border: 1px solid rgba(0, 240, 255, 0.22);
  box-shadow: 0 0 24px rgba(0, 240, 255, 0.12);
  transition:
    transform 0.3s,
    box-shadow 0.3s;
}

.app-card:hover .icon {
  transform: scale(1.1) rotate(-4deg);
  box-shadow: 0 0 32px rgba(0, 240, 255, 0.28);
}

.app-card h2 {
  margin: 0;
  font-size: clamp(20px, 2.5vw, 24px);
  font-weight: 700;
  color: var(--color-heading);
  letter-spacing: -0.01em;
}

.app-card p {
  margin-top: 12px;
  font-size: 15px;
  color: var(--tech-text-muted);
  line-height: 1.6;
}

@media (max-width: 640px) {
  .home {
    justify-content: flex-start;
    padding-top: 72px;
  }

  .app-card {
    flex: 1 1 100%;
    max-width: none;
    padding: 28px 24px;
  }

  .icon {
    width: 64px;
    height: 64px;
    font-size: 30px;
  }
}
</style>
