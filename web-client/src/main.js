import { createApp } from 'vue'
import { router } from './router'
import AppLayout from './layouts/AppLayout.vue'

createApp(AppLayout).use(router).mount('#app')
