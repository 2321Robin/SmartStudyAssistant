import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import NoteView from '../views/NoteView.vue'
import PlanGenerateView from '../views/PlanGenerateView.vue'
import PlanListView from '../views/PlanListView.vue'
import ResourceView from '../views/ResourceView.vue'

const tokenKey = 'smartstudy.auth'

const routes = [
  { path: '/', redirect: '/plans/generate' },
  { path: '/login', component: LoginView, meta: { public: true } },
  { path: '/plans/generate', component: PlanGenerateView },
  { path: '/plans', component: PlanListView },
  { path: '/resources', component: ResourceView },
  { path: '/notes', component: NoteView }
]

export const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  if (to.meta.public) {
    return true
  }

  return window.localStorage.getItem(tokenKey) ? true : '/login'
})
