import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'
import path from 'path'

export default defineConfig({
  plugins: [vue({
    template: {
      compilerOptions: {
        // 让 Vue 忽略 emoji-picker，自定义元素不会报错
        isCustomElement: tag => tag === 'emoji-picker'
      }
    }
  })],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080', //  Spring Boot 后端地址
        changeOrigin: true,
      
      }
    }
  }
})
