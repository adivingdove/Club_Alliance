// 浏览历史管理工具
const HISTORY_KEY = 'forum_browsing_history'
const MAX_HISTORY_SIZE = 50 // 最大历史记录数量

// 获取浏览历史
export const getBrowsingHistory = () => {
  try {
    const history = localStorage.getItem(HISTORY_KEY)
    return history ? JSON.parse(history) : []
  } catch (error) {
    console.error('获取浏览历史失败:', error)
    return []
  }
}

// 添加浏览记录
export const addBrowsingHistory = (post) => {
  try {
    const history = getBrowsingHistory()
    
    // 检查是否已存在相同帖子
    const existingIndex = history.findIndex(item => item.id === post.id)
    if (existingIndex !== -1) {
      // 如果已存在，移除旧记录
      history.splice(existingIndex, 1)
    }
    
    // 添加新记录到开头
    const newRecord = {
      ...post,
      browseTime: new Date().toISOString()
    }
    history.unshift(newRecord)
    
    // 限制历史记录数量
    if (history.length > MAX_HISTORY_SIZE) {
      history.splice(MAX_HISTORY_SIZE)
    }
    
    localStorage.setItem(HISTORY_KEY, JSON.stringify(history))
  } catch (error) {
    console.error('添加浏览历史失败:', error)
  }
}

// 清除浏览历史
export const clearBrowsingHistory = () => {
  try {
    localStorage.removeItem(HISTORY_KEY)
  } catch (error) {
    console.error('清除浏览历史失败:', error)
  }
}

// 移除特定浏览记录
export const removeBrowsingHistory = (postId) => {
  try {
    const history = getBrowsingHistory()
    const filteredHistory = history.filter(item => item.id !== postId)
    localStorage.setItem(HISTORY_KEY, JSON.stringify(filteredHistory))
  } catch (error) {
    console.error('移除浏览历史失败:', error)
  }
}

// 格式化浏览时间
export const formatBrowseTime = (timeString) => {
  const now = new Date()
  const browseTime = new Date(timeString)
  const diffMs = now - browseTime
  const diffMinutes = Math.floor(diffMs / (1000 * 60))
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60))
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))
  
  if (diffMinutes < 1) {
    return '刚刚'
  } else if (diffMinutes < 60) {
    return `${diffMinutes}分钟前`
  } else if (diffHours < 24) {
    return `${diffHours}小时前`
  } else if (diffDays < 7) {
    return `${diffDays}天前`
  } else {
    return browseTime.toLocaleDateString('zh-CN')
  }
} 