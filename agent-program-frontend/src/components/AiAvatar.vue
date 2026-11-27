<template>
  <svg class="ai-avatar" viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="aiAvatarGrad" x1="0" y1="0" x2="64" y2="64" gradientUnits="userSpaceOnUse">
        <stop offset="0" stop-color="#00f0ff" />
        <stop offset="1" stop-color="#2a6fff" />
      </linearGradient>
    </defs>
    <rect x="10" y="16" width="44" height="34" rx="10" fill="url(#aiAvatarGrad)" />
    <rect x="16" y="24" width="32" height="18" rx="6" fill="#050b14" />
    <circle cx="24" cy="31" r="3.5" fill="#00f0ff" />
    <circle cx="40" cy="31" r="3.5" fill="#00f0ff" />
    <rect x="26" y="37" width="12" height="2.5" rx="1.25" fill="#00f0ff" />
    <path
      d="M20 16v-4a4 4 0 0 1 4-4h16a4 4 0 0 1 4 4v4"
      fill="none"
      stroke="url(#aiAvatarGrad)"
      stroke-width="3"
      stroke-linecap="round"
    />
  </svg>
</template>

<style scoped>
.ai-avatar {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
}
</style>
