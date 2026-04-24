<script setup>
import { onMounted, ref } from 'vue'
import { deletePlan, listPlans, updateTaskStatus } from '../api/plan'

function statusLabel(status) {
  return status === 'COMPLETED' ? '已完成' : '进行中'
}

const plans = ref([])
const error = ref('')
const loading = ref(false)

async function loadPlans() {
  loading.value = true
  error.value = ''

  try {
    plans.value = await listPlans()
  } catch (requestError) {
    error.value = requestError.message
  } finally {
    loading.value = false
  }
}

async function completeTask(planId, taskId) {
  try {
    const updated = await updateTaskStatus(planId, taskId, 'COMPLETED')
    plans.value = plans.value.map((plan) => (plan.id === updated.id ? updated : plan))
  } catch (requestError) {
    error.value = requestError.message
  }
}

async function removePlan(planId) {
  try {
    await deletePlan(planId)
    plans.value = plans.value.filter((plan) => plan.id !== planId)
  } catch (requestError) {
    error.value = requestError.message
  }
}

onMounted(loadPlans)
</script>

<template>
  <section class="panel">
    <div class="section-header">
      <div>
        <h2>学习计划列表</h2>
        <p>查看已生成的学习计划，并支持完成任务和删除计划。</p>
      </div>
      <button type="button" @click="loadPlans">刷新</button>
    </div>

    <p v-if="loading">加载中...</p>
    <p v-if="error" class="error-text">{{ error }}</p>
    <p v-if="!loading && plans.length === 0">暂无计划，先去生成一个。</p>

    <article v-for="plan in plans" :key="plan.id" class="plan-card">
      <div class="section-header">
        <div>
          <h3>{{ plan.title }}</h3>
          <p>周期：{{ plan.durationDays }} 天 / {{ plan.dailyHours }} 小时，状态：{{ statusLabel(plan.status) }}</p>
        </div>
        <button type="button" class="danger-button" @click="removePlan(plan.id)">删除</button>
      </div>

      <ul>
        <li v-for="task in plan.tasks" :key="task.id" class="task-row">
          <span>第{{ task.stage }}阶段 - {{ task.title }} - {{ statusLabel(task.status) }}</span>
          <button v-if="task.status !== 'COMPLETED'" type="button" @click="completeTask(plan.id, task.id)">标记完成</button>
        </li>
      </ul>
    </article>
  </section>
</template>

<style scoped>
.panel,
.plan-card {
  background: white;
  border-radius: 18px;
  padding: 24px;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.08);
}

.plan-card {
  margin-top: 18px;
}

.section-header,
.task-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
}

.section-header h2,
.section-header h3,
.section-header p {
  margin: 0;
}

.section-header p {
  margin-top: 6px;
  color: #64748b;
}

ul {
  padding-left: 18px;
}

.task-row {
  margin-top: 10px;
}

.danger-button {
  background: #dc2626;
}

.error-text {
  color: #b91c1c;
}
</style>
