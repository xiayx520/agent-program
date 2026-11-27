<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

interface Particle {
  x: number
  y: number
  vx: number
  vy: number
  radius: number
  alpha: number
}

const canvasRef = ref<HTMLCanvasElement | null>(null)
let ctx: CanvasRenderingContext2D | null = null
let rafId = 0
let particles: Particle[] = []
let width = 0
let height = 0

const initParticles = (count: number) => {
  particles = []
  for (let i = 0; i < count; i++) {
    particles.push({
      x: Math.random() * width,
      y: Math.random() * height,
      vx: (Math.random() - 0.5) * 0.35,
      vy: (Math.random() - 0.5) * 0.35,
      radius: Math.random() * 1.2 + 0.8,
      alpha: Math.random() * 0.25 + 0.2
    })
  }
}

const resize = () => {
  const canvas = canvasRef.value
  if (!canvas) return
  const dpr = window.devicePixelRatio || 1
  const rect = canvas.getBoundingClientRect()
  width = rect.width
  height = rect.height
  canvas.width = Math.floor(width * dpr)
  canvas.height = Math.floor(height * dpr)
  ctx = canvas.getContext('2d')
  if (ctx) {
    ctx.scale(dpr, dpr)
  }
  const isMobile = width < 640
  initParticles(isMobile ? 24 : 48)
}

const draw = () => {
  if (!ctx) return
  ctx.clearRect(0, 0, width, height)

  const lineDistance = 120
  const maxConnections = 3

  // draw connections first so they appear behind particles
  for (let i = 0; i < particles.length; i++) {
    const p1 = particles[i]!
    let connections = 0
    for (let j = i + 1; j < particles.length; j++) {
      const p2 = particles[j]!
      const dx = p1.x - p2.x
      const dy = p1.y - p2.y
      const dist = Math.hypot(dx, dy)
      if (dist < lineDistance && connections < maxConnections) {
        connections++
        const alpha = (1 - dist / lineDistance) * 0.18
        ctx.beginPath()
        ctx.strokeStyle = `rgba(0, 240, 255, ${alpha})`
        ctx.lineWidth = 0.8
        ctx.moveTo(p1.x, p1.y)
        ctx.lineTo(p2.x, p2.y)
        ctx.stroke()
      }
    }
  }

  // draw particles
  for (const p of particles) {
    p.x += p.vx
    p.y += p.vy

    if (p.x < 0 || p.x > width) p.vx *= -1
    if (p.y < 0 || p.y > height) p.vy *= -1

    ctx.beginPath()
    ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2)
    ctx.fillStyle = `rgba(0, 240, 255, ${p.alpha})`
    ctx.fill()
  }

  rafId = requestAnimationFrame(draw)
}

onMounted(() => {
  resize()
  draw()
  window.addEventListener('resize', resize)
})

onUnmounted(() => {
  window.removeEventListener('resize', resize)
  cancelAnimationFrame(rafId)
})
</script>

<template>
  <canvas ref="canvasRef" class="tech-background" aria-hidden="true" />
</template>

<style scoped>
.tech-background {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
  opacity: 0.85;
}
</style>
