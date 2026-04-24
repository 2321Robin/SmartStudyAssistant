<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { login, register } from '../api/auth'

const router = useRouter()
const mode = ref('login')
const form = reactive({
  username: '',
  password: ''
})
const message = ref('')
const error = ref('')
const loading = ref(false)

async function submit() {
  loading.value = true
  error.value = ''
  message.value = ''

  try {
    const action = mode.value === 'login' ? login : register
    const auth = await action(form)
    message.value = `${mode.value === 'login' ? '登录' : '注册'}成功，当前用户：${auth.username}`
    router.push('/plans/generate')
  } catch (requestError) {
    error.value = requestError.message
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section class="card auth-card">
    <div class="card-header">
      <div>
        <h2>账号登录</h2>
        <p>先登录，再体验学习计划、资源和笔记广场。</p>
      </div>
      <div class="mode-toggle">
        <button type="button" :class="{ secondary: mode !== 'login' }" @click="mode = 'login'">登录</button>
        <button type="button" :class="{ secondary: mode !== 'register' }" @click="mode = 'register'">注册</button>
      </div>
    </div>

    <form class="form-grid" @submit.prevent="submit">
      <label for="username">用户名</label>
      <input id="username" v-model.trim="form.username" autocomplete="username" placeholder="请输入用户名" required />

      <label for="password">密码</label>
      <input
        id="password"
        v-model="form.password"
        type="password"
        autocomplete="current-password"
        placeholder="至少 8 位"
        minlength="8"
        required
      />

      <button type="submit" :disabled="loading">
        {{ loading ? '提交中...' : mode === 'login' ? '登录' : '注册并登录' }}
      </button>
    </form>

    <p v-if="message" class="success-text">{{ message }}</p>
    <p v-if="error" class="error-text">{{ error }}</p>
  </section>
</template>

<style scoped>
.card {
  max-width: 560px;
  padding: 24px;
  border-radius: 18px;
  background: white;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.08);
}

.auth-card {
  margin: 40px auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.card-header h2,
.card-header p {
  margin: 0;
}

.card-header p {
  margin-top: 8px;
  color: #64748b;
}

.mode-toggle {
  display: flex;
  gap: 8px;
}

.secondary {
  background: #cbd5e1;
  color: #0f172a;
}

.form-grid {
  display: grid;
  gap: 10px;
  margin-top: 24px;
}

label {
  font-weight: 700;
}

input {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #cbd5e1;
  border-radius: 10px;
}

.success-text {
  color: #047857;
}

.error-text {
  color: #b91c1c;
}
</style>
