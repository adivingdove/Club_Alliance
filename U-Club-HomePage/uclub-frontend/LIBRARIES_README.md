# 库配置说明

本项目已成功集成了以下三个重要库：

## 1. Axios (HTTP请求库)

### 配置文件位置
- `src/utils/request.js`

### 主要功能
- 创建了axios实例，配置了基础URL和超时时间
- 实现了请求拦截器，自动添加token认证
- 实现了响应拦截器，统一处理错误状态码
- 封装了常用的HTTP方法（GET、POST、PUT、DELETE）

### 使用方法
```javascript
import { http } from '@/utils/request'

// GET请求
const data = await http.get('/api/users', { page: 1 })

// POST请求
const result = await http.post('/api/users', { name: '张三', age: 25 })

// PUT请求
const updated = await http.put('/api/users/1', { name: '李四' })

// DELETE请求
await http.delete('/api/users/1')
```

## 2. ECharts (图表库)

### 配置文件位置
- `src/components/ECharts.vue`

### 主要功能
- 封装了ECharts为Vue组件
- 支持动态更新图表配置
- 自动响应窗口大小变化
- 支持主题切换
- 提供图表点击事件

### 使用方法
```vue
<template>
  <ECharts 
    :option="chartOption" 
    height="400px"
    @chart-click="onChartClick"
    @chart-ready="onChartReady"
  />
</template>

<script>
import ECharts from '@/components/ECharts.vue'

export default {
  components: { ECharts },
  setup() {
    const chartOption = ref({
      title: { text: '我的图表' },
      xAxis: { type: 'category', data: ['A', 'B', 'C'] },
      yAxis: { type: 'value' },
      series: [{ type: 'bar', data: [10, 20, 30] }]
    })
    
    return { chartOption }
  }
}
</script>
```

## 3. Vuex Store (状态管理)

### 配置文件位置
- `src/store/index.js`

### 主要功能
- 全局状态管理
- 用户信息管理
- 俱乐部数据管理
- 加载状态和错误处理
- 异步操作处理

### 使用方法
```javascript
import { useStore } from 'vuex'
import { computed } from 'vue'

export default {
  setup() {
    const store = useStore()
    
    // 获取状态
    const user = computed(() => store.getters.getUser)
    const clubs = computed(() => store.getters.getClubs)
    const loading = computed(() => store.getters.getLoading)
    
    // 触发actions
    const fetchUser = () => store.dispatch('fetchUser')
    const fetchClubs = () => store.dispatch('fetchClubs')
    
    return { user, clubs, loading, fetchUser, fetchClubs }
  }
}
```

## 示例组件

查看 `src/components/ExampleUsage.vue` 了解完整的使用示例。

## 环境变量配置

在项目根目录创建 `.env` 文件：
```
VITE_API_BASE_URL=http://localhost:3000/api
VITE_APP_TITLE=我的Vue应用
```

## 注意事项

1. **Axios**: 确保在发送请求前配置正确的API基础URL
2. **ECharts**: 图表组件会自动处理内存清理，无需手动销毁
3. **Vuex**: Store已集成到main.js中，可在任何组件中使用

## 下一步

1. 根据实际需求修改API接口地址
2. 自定义ECharts主题和样式
3. 扩展Store中的状态和操作
4. 添加更多的图表类型和交互功能 