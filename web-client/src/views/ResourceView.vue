<script setup>
import { onMounted, ref } from 'vue'
import { searchResources } from '../api/resource'

function resourceTypeLabel(type) {
  if (type === 'VIDEO') return '视频'
  if (type === 'DOCUMENT') return '文档'
  return '文章'
}

const keyword = ref('')
const resources = ref([])
const loading = ref(false)
const error = ref('')

async function runSearch() {
  loading.value = true
  error.value = ''

  try {
    resources.value = await searchResources(keyword.value)
  } catch (requestError) {
    error.value = requestError.message
  } finally {
    loading.value = false
  }
}

onMounted(runSearch)
</script>

<template>
  <section class="panel">
    <h2>学习资源搜索</h2>
    <p>按关键词查找课程相关的学习资源，便于快速补充资料。</p>

    <form class="search-row" @submit.prevent="runSearch">
      <input v-model.trim="keyword" placeholder="输入 Spring Cloud、Gateway 等关键词" />
      <button type="submit">搜索</button>
    </form>

    <p v-if="loading">搜索中...</p>
    <p v-if="error" class="error-text">{{ error }}</p>
    <p v-if="!loading && !error && resources.length === 0">未找到相关资源，可以换个关键词再试试。</p>

    <article v-for="resource in resources" :key="resource.id" class="resource-card">
      <h3>{{ resource.title }}</h3>
      <p class="resource-description">{{ resource.description }}</p>
      <p class="resource-meta">
        <span>文章来源：{{ resource.source }}</span>
        <span>适合阶段：{{ resource.level }}</span>
      </p>
      <p>{{ resourceTypeLabel(resource.type) }} | {{ resource.tags.join(', ') }}</p>
      <a :href="resource.url" target="_blank" rel="noreferrer">{{ resource.url }}</a>
    </article>
  </section>
</template>

<style scoped>
.panel,
.resource-card {
  background: white;
  border-radius: 18px;
  padding: 24px;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.08);
}

.search-row {
  display: flex;
  gap: 12px;
}

input {
  flex: 1;
  padding: 12px 14px;
  border: 1px solid #cbd5e1;
  border-radius: 10px;
}

.resource-card {
  margin-top: 18px;
}

.resource-description {
  color: #475569;
  line-height: 1.7;
  margin-bottom: 12px;
}

.resource-meta {
  color: #334155;
  font-size: 14px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 10px;
}

.error-text {
  color: #b91c1c;
}
</style>
