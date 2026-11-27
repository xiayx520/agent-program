<template>
  <div class="home">
    <h1 class="home-title">智能应用中心</h1>
    <p class="home-subtitle">选择下方应用开始体验</p>
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
  padding: 48px;
  text-align: center;
  background: linear-gradient(180deg, #f9fafb 0%, #ffffff 100%);
}

.home-title {
  margin: 0;
  font-size: 36px;
  font-weight: 700;
  color: #111827;
}

.home-subtitle {
  margin-top: 12px;
  font-size: 16px;
  color: #6b7280;
}

.apps {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 32px;
  margin-top: 48px;
}

.app-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 280px;
  padding: 40px 32px;
  color: inherit;
  text-decoration: none;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 20px;
  transition:
    transform 0.2s,
    box-shadow 0.2s;
}

.app-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
}

.icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.app-card h2 {
  margin: 0;
  font-size: 22px;
  color: #111827;
}

.app-card p {
  margin-top: 10px;
  font-size: 14px;
  color: #6b7280;
  line-height: 1.5;
}
</style>
