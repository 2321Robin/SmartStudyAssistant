<script setup>
import { reactive, ref } from 'vue'
import { generatePlan } from '../api/plan'

function statusLabel(status) {
  return status === 'COMPLETED' ? '已完成' : '进行中'
}

const form = reactive({
  goal: '30天学完Spring Cloud',
  durationDays: 30,
  dailyHours: 2
})
const generatedPlan = ref(null)
const error = ref('')
const loading = ref(false)

async function submit() {
  loading.value = true
  error.value = ''

  try {
    generatedPlan.value = await generatePlan(form)
  } catch (requestError) {
    error.value = requestError.message
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section class="panel">
    <h2>生成学习计划</h2>
    <p>填写学习目标、学习周期和每日投入时间，系统会生成阶段性学习计划。</p>

    <form class="form-grid" @submit.prevent="submit">
      <label>
        学习目标
        <input v-model.trim="form.goal" placeholder="例如：30天学完Spring Cloud" required />
      </label>

      <label>
        学习天数
        <input v-model.number="form.durationDays" type="number" min="1" max="365" required />
      </label>

      <label>
        每日时长
        <input v-model.number="form.dailyHours" type="number" min="1" max="24" required />
      </label>

      <button type="submit">{{ loading ? '生成中...' : '生成计划' }}</button>
    </form>

    <p v-if="error" class="error-text">{{ error }}</p>

    <article v-if="generatedPlan" class="result-card">
      <h3>{{ generatedPlan.title }}</h3>
      <p>状态：{{ statusLabel(generatedPlan.status) }}</p>
      <ul>
        <li v-for="task in generatedPlan.tasks" :key="task.id">
          第{{ task.stage }}阶段 - {{ task.title }} - {{ statusLabel(task.status) }}
        </li>
      </ul>
    </article>
  </section>
</template>

<style scoped>
.panel,
.result-card {
  background: white;
  border-radius: 18px;
  padding: 24px;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.08);
}

.form-grid {
  display: grid;
  gap: 14px;
}

label {
  display: grid;
  gap: 8px;
}

input {
  padding: 12px 14px;
  border: 1px solid #cbd5e1;
  border-radius: 10px;
}

.result-card {
  margin-top: 20px;
}

.error-text {
  color: #b91c1c;
}
</style>
