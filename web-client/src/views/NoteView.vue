<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { createNote, listAllNotes, listNoteSquare } from '../api/note'

const PREVIEW_LENGTH = 90
const INITIAL_MY_NOTES_COUNT = 5

const form = reactive({
  title: '',
  content: '',
  publicVisible: true
})
const myNotes = ref([])
const notes = ref([])
const loading = ref(false)
const error = ref('')
const expandedNoteIds = ref([])
const myNotesExpanded = ref(false)

const visibleMyNotes = computed(() => {
  if (myNotesExpanded.value || myNotes.value.length <= INITIAL_MY_NOTES_COUNT) {
    return myNotes.value
  }
  return myNotes.value.slice(0, INITIAL_MY_NOTES_COUNT)
})

function isExpanded(noteId) {
  return expandedNoteIds.value.includes(noteId)
}

function toggleExpanded(noteId) {
  expandedNoteIds.value = isExpanded(noteId)
    ? expandedNoteIds.value.filter((id) => id !== noteId)
    : [...expandedNoteIds.value, noteId]
}

function previewContent(note) {
  if (isExpanded(note.id) || note.content.length <= PREVIEW_LENGTH) {
    return note.content
  }
  return `${note.content.slice(0, PREVIEW_LENGTH)}...`
}

function visibilityLabel(note) {
  return note.publicVisible ? '已发布到广场' : '仅自己可见'
}

function formatCreatedAt(value) {
  if (!value) {
    return '发布时间未知'
  }

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return '发布时间未知'
  }

  return `发布时间：${date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })}`
}

function toggleMyNotesExpanded() {
  myNotesExpanded.value = !myNotesExpanded.value
}

async function loadSquare() {
  loading.value = true
  error.value = ''

  try {
    myNotes.value = await listAllNotes()
    notes.value = await listNoteSquare()
    myNotesExpanded.value = false
    expandedNoteIds.value = []
  } catch (requestError) {
    error.value = requestError.message
  } finally {
    loading.value = false
  }
}

async function submit() {
  try {
    await createNote(form)
    form.title = ''
    form.content = ''
    form.publicVisible = true
    await loadSquare()
  } catch (requestError) {
    error.value = requestError.message
  }
}

onMounted(loadSquare)
</script>

<template>
  <section class="panel notes-layout">
    <article class="note-form card">
      <h2>创建笔记</h2>
      <form class="form-grid" @submit.prevent="submit">
        <label>
          标题
          <input v-model.trim="form.title" required />
        </label>

        <label>
          内容
          <textarea v-model.trim="form.content" rows="6" required />
        </label>

        <label class="checkbox-row">
          <input v-model="form.publicVisible" type="checkbox" />
          发布到笔记广场
        </label>

        <button type="submit">发布笔记</button>
      </form>
    </article>

    <article class="card my-notes-card">
      <div class="section-header">
        <div>
          <h2>我的笔记（{{ myNotes.length }}）</h2>
          <p>查看自己保存的全部笔记，区分草稿和已公开内容。</p>
        </div>
        <button
          v-if="myNotes.length > INITIAL_MY_NOTES_COUNT"
          type="button"
          class="ghost-action section-action"
          @click="toggleMyNotesExpanded"
        >
          {{ myNotesExpanded ? '收起' : '展开全部' }}
        </button>
      </div>

      <p v-if="!loading && myNotes.length === 0" class="empty-text">你还没有保存任何笔记，先写一篇试试吧。</p>

      <article v-for="note in visibleMyNotes" :key="`mine-${note.id}`" class="note-card">
        <div class="note-topbar">
          <span :class="['note-badge', note.publicVisible ? 'is-public' : 'is-private']">{{ visibilityLabel(note) }}</span>
          <span class="note-meta">{{ formatCreatedAt(note.createdAt) }}</span>
        </div>
        <h3>{{ note.title }}</h3>
        <p class="note-content">{{ previewContent(note) }}</p>
        <div class="note-actions">
          <button
            v-if="note.content.length > PREVIEW_LENGTH"
            type="button"
            class="ghost-action"
            @click="toggleExpanded(note.id)"
          >
            {{ isExpanded(note.id) ? '收起' : '展开全文' }}
          </button>
        </div>
      </article>
    </article>

    <article class="card square-card">
      <div class="section-header">
        <div>
          <h2>笔记广场（{{ notes.length }}）</h2>
          <p>浏览大家公开分享的学习笔记，整理思路与经验。</p>
        </div>
        <button type="button" class="refresh-button" @click="loadSquare">刷新</button>
      </div>

      <p v-if="loading">加载中...</p>
      <p v-if="error" class="error-text">{{ error }}</p>
      <p v-if="!loading && notes.length === 0" class="empty-text">还没有公开笔记，发布第一篇来开启分享吧。</p>

      <div v-if="notes.length > 0" class="square-grid">
        <article v-for="note in notes" :key="note.id" class="note-card">
          <div class="note-topbar">
            <span class="note-badge">公开笔记</span>
            <span class="note-meta">{{ formatCreatedAt(note.createdAt) }}</span>
          </div>
          <h3>{{ note.title }}</h3>
          <p class="note-content">{{ previewContent(note) }}</p>
          <div class="note-actions">
            <button
              v-if="note.content.length > PREVIEW_LENGTH"
              type="button"
              class="ghost-action"
              @click="toggleExpanded(note.id)"
            >
              {{ isExpanded(note.id) ? '收起' : '展开全文' }}
            </button>
          </div>
        </article>
      </div>
    </article>
  </section>
</template>

<style scoped>
.notes-layout {
  display: grid;
  grid-template-columns: minmax(0, 380px) minmax(0, 1fr);
  gap: 20px;
  background: transparent;
  box-shadow: none;
  padding: 0;
}

.card {
  background: white;
  border-radius: 18px;
  padding: 24px;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.08);
}

.square-card {
  grid-column: 1 / -1;
}

.square-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-top: 16px;
}

.form-grid {
  display: grid;
  gap: 14px;
}

label {
  display: grid;
  gap: 8px;
}

input,
textarea {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #cbd5e1;
  border-radius: 10px;
}

.checkbox-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.checkbox-row input {
  width: auto;
}

.section-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.section-header h2,
.section-header p,
.note-card h3,
.note-card p {
  margin: 0;
}

.section-header p {
  margin-top: 6px;
  color: #64748b;
}

.note-card {
  padding: 18px;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
}

.note-topbar,
.note-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.note-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  background: #dbeafe;
  color: #1d4ed8;
  font-size: 13px;
  font-weight: 700;
}

.note-badge.is-public {
  background: #dbeafe;
  color: #1d4ed8;
}

.note-badge.is-private {
  background: #ede9fe;
  color: #6d28d9;
}

.note-meta {
  color: #64748b;
  font-size: 13px;
}

.note-card h3 {
  margin: 14px 0 10px;
}

.note-content {
  color: #334155;
  line-height: 1.7;
  white-space: pre-wrap;
}

.ghost-action {
  padding: 0;
  border: none;
  background: transparent;
  color: #2563eb;
  font-weight: 700;
}

.refresh-button {
  border: 1px solid #bfdbfe;
  background: linear-gradient(180deg, #eff6ff 0%, #dbeafe 100%);
  color: #1d4ed8;
  border-radius: 999px;
  padding: 10px 16px;
  font-weight: 700;
  box-shadow: 0 8px 18px rgba(37, 99, 235, 0.12);
  transition: transform 0.18s ease, box-shadow 0.18s ease, background 0.18s ease;
}

.refresh-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 22px rgba(37, 99, 235, 0.16);
  background: linear-gradient(180deg, #dbeafe 0%, #bfdbfe 100%);
}

.refresh-button:active {
  transform: translateY(0);
  box-shadow: 0 6px 14px rgba(37, 99, 235, 0.14);
}

.section-action {
  flex-shrink: 0;
  align-self: flex-start;
  margin-top: 4px;
}

.empty-text {
  color: #64748b;
}

.error-text {
  color: #b91c1c;
}

@media (max-width: 900px) {
  .notes-layout {
    grid-template-columns: 1fr;
  }

  .square-grid {
    grid-template-columns: 1fr;
  }
}
</style>
