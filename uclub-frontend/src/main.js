import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

const app = createApp(App)

// 使用 Element Plus
app.use(ElementPlus)

// 使用 Vue Router
app.use(router)

// 挂载应用
app.mount('#app')
