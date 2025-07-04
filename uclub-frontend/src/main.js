import { createApp } from 'vue'
import App from './App.vue'
import store from './store'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import { MotionPlugin } from '@vueuse/motion'
import 'emoji-picker-element'
import { Quill } from '@vueup/vue-quill'
import ImageResize from 'quill-image-resize-module-plus'

Quill.register('modules/imageResize', ImageResize)

const app = createApp(App)

// 初始化应用状态
store.dispatch('initializeApp')

// 注册 Vuex
app.use(store)

// 使用 Element Plus
app.use(ElementPlus)

// 使用 Vue Router
app.use(router)
app.use(MotionPlugin)
// 挂载应用
app.mount('#app')
