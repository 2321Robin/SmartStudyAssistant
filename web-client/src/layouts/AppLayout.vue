<script setup>
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { authState, logout } from '../api/auth'

const route = useRoute()
const router = useRouter()

const navItems = [
  { to: '/login', label: '登录' },
  { to: '/plans/generate', label: '生成计划' },
  { to: '/plans', label: '学习计划' },
  { to: '/resources', label: '学习资源' },
  { to: '/notes', label: '学习笔记' }
]

const isLoginRoute = computed(() => route.path === '/login')

function handleLogout() {
  logout()
  router.push('/login')
}
</script>

<template>
  <div class="shell">
    <header class="topbar">
      <div>
        <h1>智能学习助手</h1>
        <p>面向课程项目演示的智能学习协作平台。</p>
      </div>
      <div class="auth-bar">
        <span v-if="authState">{{ authState.username }}</span>
        <button v-if="authState && !isLoginRoute" class="ghost-button" type="button" @click="handleLogout">
          退出登录
        </button>
      </div>
    </header>

    <div class="body">
      <nav class="sidebar">
        <RouterLink
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          class="nav-link"
          :class="{ active: route.path === item.to }"
        >
          {{ item.label }}
        </RouterLink>
      </nav>

      <main class="content">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<style scoped>
:global(body) {
  margin: 0;
  font-family: Arial, Helvetica, sans-serif;
  background: #f3f6fb;
  color: #1f2937;
}

:global(*) {
  box-sizing: border-box;
}

.shell {
  min-height: 100vh;
}

.topbar {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 24px 32px;
  background: #111827;
  color: #f9fafb;
}

.topbar h1,
.topbar p {
  margin: 0;
}

.topbar p {
  margin-top: 6px;
  color: #cbd5e1;
}

.auth-bar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.body {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  min-height: calc(100vh - 96px);
}

.sidebar {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 24px 16px;
  background: #ffffff;
  border-right: 1px solid #dbe3ef;
}

.nav-link {
  padding: 12px 14px;
  border-radius: 10px;
  color: #334155;
  text-decoration: none;
}

.nav-link.active,
.nav-link:hover {
  background: #e0e7ff;
  color: #312e81;
}

.content {
  padding: 24px;
}

.ghost-button {
  border: 1px solid #475569;
  background: transparent;
  color: inherit;
}

button,
input,
textarea {
  font: inherit;
}

button {
  cursor: pointer;
  border: none;
  border-radius: 10px;
  padding: 10px 14px;
  background: #2563eb;
  color: white;
}

@media (max-width: 900px) {
  .body {
    grid-template-columns: 1fr;
  }

  .sidebar {
    flex-direction: row;
    overflow-x: auto;
    border-right: none;
    border-bottom: 1px solid #dbe3ef;
  }

  .topbar {
    flex-direction: column;
  }
}
</style>
