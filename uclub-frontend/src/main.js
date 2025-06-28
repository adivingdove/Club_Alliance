import { createApp } from 'vue'
import App from './App.vue'
import store from './store'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

const app = createApp(App)

// 初始化应用状态
store.dispatch('initializeApp')

// 注册 Vuex
app.use(store)

// 使用 Element Plus
app.use(ElementPlus)

// 使用 Vue Router
app.use(router)

// 挂载应用
app.mount('#app')
