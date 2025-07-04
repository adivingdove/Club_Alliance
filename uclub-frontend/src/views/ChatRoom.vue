<template>
  <div class="chatroom-wrapper">
    <!-- 左侧侧边栏 -->
    <div class="sidebar">
      <h3>聊天室列表</h3>
      <el-menu
        class="chatroom-menu"
        :default-active="currentRoom"
        @select="switchRoom"
      >
        <el-menu-item index="public">🌐 通用聊天室</el-menu-item>
        <el-menu-item
          v-for="club in myClubs"
          :key="club.id"
          :index="'club-' + club.id"
        >
          🏷 {{ club.name }}
        </el-menu-item>
      </el-menu>

      <!-- 在线用户列表 -->
      <div class="online-users">
        <h3>在线成员</h3>
        <div v-if="onlineUsers.length === 0">暂无成员在线</div>
        <div
          v-for="(user, idx) in onlineUsers"
          :key="idx"
          class="online-user"
          style="display: flex; align-items: center; margin-bottom: 8px;"
        >
          <el-avatar
            :src="formatAvatar(user.avatar)"
            :size="24"
            style="margin-right: 8px"
          />
          <span>{{ user.nickname }}</span>
        </div>
      </div>
    </div>

    <!-- 聊天区域 -->
    <div class="chatroom-container">
      <h2>{{ currentRoomLabel }}</h2>

      <!-- 聊天记录 -->
      <div class="chat-log">
        <div
          v-for="(msg, idx) in messages"
          :key="idx"
          :class="[
            'chat-message',
            msg.role === '系统'
              ? 'system-message'
              : msg.sender === currentUser.nickname
              ? 'my-message'
              : 'other-message'
          ]"
        >
          <div class="chat-bubble">
            <div
              class="user-info"
              style="display: flex; align-items: center; margin-bottom: 5px;"
              v-if="msg.role !== '系统'"
            >
              <el-avatar
                :src="formatAvatar(msg.avatar) || defaultAvatar"
                :size="30"
                style="margin-right: 8px"
              />
              <span class="nickname" :class="msg.role">
                {{ msg.sender }}
                <span v-if="msg.role !== '成员'" class="badge">
                  {{ roleMap[msg.role] }}
                </span>
              </span>
            </div>
            <div class="content" v-html="formatMessage(msg.content)"></div>
            <div class="time">{{ formatTime(msg.time) }}</div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-inputs">
        <el-input
          ref="inputRef"
          v-model="message"
          placeholder="说点什么..."
          @keyup.enter="sendMessage"
          style="flex: 1; margin: 0 10px"
        />
        <el-button type="primary" @click="sendMessage">发送</el-button>
        <el-button @click="showEmoji = !showEmoji">😊</el-button>
      </div>

      <!-- Emoji 选择器 -->
      <div v-if="showEmoji" class="emoji-picker-wrapper">
        <emoji-picker @emoji-click="addEmoji" />
      </div>
    </div>
  </div>
</template>


<script setup>
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue'
import SockJS from 'sockjs-client/dist/sockjs.min.js'
import { Client } from '@stomp/stompjs'
import { getMyClubs, getProfileInfo } from '@/api/profileApi'

import 'emoji-picker-element'
import axios from 'axios'

const onlineUsers = ref([])

const fetchOnlineUsers = async () => {
  const room = currentRoom.value
  const url = `${apiBaseUrl}/api/chat/online/${room}`
  const token = localStorage.getItem('token')

  console.log(`[Chat]  正在请求房间 ${room} 的在线用户...`, url)

  try {
    const res = await axios.get(url, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })

    console.log(`[Chat]  成功获取在线用户 (${res.data.length}人):`, res.data)

    onlineUsers.value = res.data
  } catch (err) {
    console.error(`[Chat]  获取房间 ${room} 的在线用户失败:`, err)
    if (err.response) {
      console.error('[Chat] 服务器响应:', err.response.status, err.response.data)
    } else if (err.request) {
      console.error('[Chat] 无服务器响应，请检查网络或后端:', err.request)
    } else {
      console.error('[Chat] 请求配置错误:', err.message)
    }
  }
}


// 当前用户信息
const currentUser = ref({ nickname: '', avatarUrl: '' })
// 消息相关
const messages = ref([])
const message = ref('')
const inputRef = ref(null)
const showEmoji = ref(false)
const defaultAvatar = 'https://cdn-icons-png.flaticon.com/512/149/149071.png' // 默认头像地址

// 当前聊天室标识
const currentRoom = ref('public') // 默认通用聊天室
const currentRoomLabel = computed(() =>
  currentRoom.value === 'public'
    ? '🟢 通用聊天室'
    : '🟢 ' + (myClubs.value.find(c => 'club-' + c.id === currentRoom.value)?.name || '')
)

// 示例社团列表（应从后端动态获取）
const myClubs = ref([])

let stompClient = null
let currentSubscription = null
let intervalId = null
const apiBaseUrl = 'http://localhost:8080' 

const formatAvatar = (avatar) => {
  if (!avatar) return defaultAvatar
  return avatar.startsWith('http') ? avatar : apiBaseUrl + avatar
}

const roleMap = {
  '社长': '👑 社长',
  '副社长': '⭐ 副社长',
  '干事': '🛠 干事',
  '成员': ''  // 成员不显示徽章
}

// 连接 WebSocket
const connect = () => {
  const token = localStorage.getItem('token')
const socket = new SockJS(`http://localhost:8080/ws-chat?token=${token}`)
  stompClient = new Client({
    webSocketFactory: () => socket,
    reconnectDelay: 5000,
    debug: (str) => console.log('[STOMP DEBUG]', str),
    connectHeaders: {
      token: localStorage.getItem('token')  
    },
    onConnect: () => {
      console.log(' WebSocket Connected')
      subscribeToRoom(currentRoom.value)
      fetchOnlineUsers()
    },
    onStompError: (frame) => {
      console.error(' STOMP error:', frame)
    },
    onWebSocketError: (err) => {
      console.error(' WebSocket error:', err)
    }
  })
  stompClient.activate()
}

// 切换聊天室
const switchRoom = (roomId) => {
  const oldRoomLabel = currentRoomLabel.value
  currentRoom.value = roomId
  messages.value = []
 messages.value.push({
    sender: '系统',
    content: `🚪 你已离开「${oldRoomLabel.replace('🟢 ', '')}」聊天室`,
    time: new Date().toISOString(),
    role: '系统'
  })
  if (stompClient?.connected) {
    subscribeToRoom(roomId)
    fetchOnlineUsers()
  }

 
}

// 订阅聊天室
const subscribeToRoom = (roomId) => {
  if (currentSubscription) {
    currentSubscription.unsubscribe()
  }

  const topic = '/topic/' + roomId
  currentSubscription = stompClient.subscribe(topic, (msg) => {
    console.log('收到消息:', msg.body)
    messages.value.push(JSON.parse(msg.body))
  })

  // 系统提示：你已进入房间
  messages.value.push({
    sender: '系统',
    avatar: '', 
     content: `🎉 欢迎来到「${currentRoomLabel.value.replace('🟢 ', '')}」聊天室！`,
    time: new Date().toISOString(),
    role: '系统'
  })
}


// 发送消息
const sendMessage = () => {
  if (!message.value.trim()) {
    console.warn(' 空消息，忽略发送')
    return
  }
  if (!stompClient || !stompClient.connected) {
    console.error(' STOMP 未连接，无法发送消息')
    return
  }

  const destination = '/app/chat.send.' + currentRoom.value
  const payload = {
    sender: currentUser.value.nickname,
    avatar: currentUser.value.headUrl || '',
    content: message.value,
    time: new Date().toISOString(),
    role: ''
  }

  try {
    stompClient.publish({
  destination,
  body: JSON.stringify(payload),
  headers: {
    token: localStorage.getItem('token')  // 从本地 token 添加进 header
  }
})
    console.log(' 发送消息成功:', payload)
    message.value = ''
  } catch (err) {
    console.error(' 消息发送失败:', err)
  }
}


// 插入 Emoji
const addEmoji = (event) => {
  const emoji = event.detail.unicode
  const inputEl = inputRef.value?.input

  if (inputEl) {
    const start = inputEl.selectionStart
    const end = inputEl.selectionEnd
    const currentValue = message.value
    message.value = currentValue.slice(0, start) + emoji + currentValue.slice(end)
    nextTick(() => {
      inputEl.selectionStart = inputEl.selectionEnd = start + emoji.length
      inputEl.focus()
    })
  } else {
    message.value += emoji
  }
}
const formatTime = (isoString) => {
  if (!isoString) return ''
  const date = new Date(isoString)
  const pad = (n) => (n < 10 ? '0' + n : n)
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

// 获取用户社团列表
const fetchMyClubs = async () => {
  try {
    const res = await getMyClubs()

    myClubs.value = res.data.data.map(club =>({
      id:club.id,
      name: `${club.name}`
    }))
    console.log('获取社团列表成功:', myClubs.value)
  } catch (err) {
    console.error('获取社团列表失败:', err)
   
  }
}

// 获取当前登录用户信息
const fetchCurrentUser = async () => {
  try {
    const res = await getProfileInfo()
    console.log('获取用户信息',res)
    currentUser.value = res.data.data
   
  } catch (err) {
    console.error('获取当前用户信息失败:', err)
  }
}

// 格式化消息换行
const formatMessage = (text) => {
  return text.replace(/(?:\r\n|\r|\n)/g, '<br>')
}

onMounted(async () => {
  await fetchCurrentUser()
  await fetchMyClubs()
  connect()
})

onUnmounted(() => {
  stompClient?.deactivate()
})

onMounted(() => {
  intervalId = setInterval(() => {
    fetchOnlineUsers()
  }, 10000) // 每 10 秒刷新一次
})

onUnmounted(() => {
  clearInterval(intervalId)
})
</script>




<style scoped>
.chatroom-wrapper {
  display: flex;
  height: 100vh;
  background: linear-gradient(to right, #f0faff, #e8f5ff);
  font-family: 'Segoe UI', sans-serif;
}

.sidebar {
  width: 240px;
  background: #ffffff;
  border-right: 1px solid #ddd;
  padding: 20px;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
}
.sidebar h3 {
  font-size: 18px;
  margin-bottom: 20px;
  color: #333;
}

.online-users {
  margin-top: 20px;
}

.online-user {
  padding: 4px 0;
  border-radius: 6px;
  transition: background-color 0.2s ease;
}
.online-user:hover {
  background-color: #f5faff;
  cursor: pointer;
}

.chatroom-menu {
  border-right: none;
}

.chatroom-menu .el-menu-item {
  padding-left: 16px !important;
  border-radius: 6px;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.chatroom-menu .el-menu-item:hover {
  background-color: #f0faff;
}

.chatroom-menu .el-menu-item.is-active {
  background-color: #cceeff;
  font-weight: bold;
}

.dot {
  display: inline-block;
  margin-left: 6px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #ff4d4f;
  animation: pulse 1.2s infinite;
  vertical-align: middle;
}
@keyframes pulse {
  0% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.4); opacity: 0.6; }
  100% { transform: scale(1); opacity: 1; }
}

.chatroom-container {
  flex: 1;
  padding: 20px 30px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chatroom-container h2 {
  margin-bottom: 15px;
  font-size: 20px;
  color: #007acc;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
  background: linear-gradient(to right, #e0f7fa, #f0faff);
  padding: 10px 16px;
  border-radius: 12px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.03);
}

.chat-log {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.03);
  margin-bottom: 20px;
}
.chat-log::-webkit-scrollbar {
  width: 8px;
}
.chat-log::-webkit-scrollbar-thumb {
  background-color: #ccc;
  border-radius: 4px;
}
.chat-log::-webkit-scrollbar-thumb:hover {
  background-color: #999;
}

.chat-message {
  display: flex;
  margin-bottom: 15px;
}

.my-message {
  justify-content: flex-end;
}

.other-message {
  justify-content: flex-start;
}

.chat-bubble {
  position: relative;
  max-width: 65%;
  background-color: #f4faff;
  padding: 12px 18px;
  border-radius: 18px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  word-break: break-word;
  transition: all 0.2s ease-in-out;
}

.my-message .chat-bubble {
  background-color: #dcfce7;
  text-align: right;
  border-top-right-radius: 6px;
}

.other-message .chat-bubble {
  border-top-left-radius: 6px;
}

.user-info .nickname {
  font-weight: 500;
  font-size: 14px;
  color: #333;
}

.content {
  margin-bottom: 6px;
  font-size: 15px;
  line-height: 1.4;
}

.time {
  font-size: 12px;
  color: #bbb;
  text-align: right;
  margin-top: 4px;
}

.chat-inputs {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 10px;
  background-color: #fff;
  border-top: 1px solid #e0e0e0;
  padding: 12px 16px;
  border-radius: 0 0 12px 12px;
  box-shadow: 0 -1px 6px rgba(0, 0, 0, 0.03);
}

.el-input {
  font-size: 15px;
  border-radius: 8px;
}

.el-button {
  border-radius: 6px;
}

.emoji-picker-wrapper {
  margin-top: 12px;
  border: 1px solid #ccc;
  border-radius: 10px;
  max-width: 320px;
  background-color: #fff;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  padding: 4px;
  z-index: 10;
}

emoji-picker {
  width: 100%;
}

.nickname.社长 .badge {
  background-color: #e67e22;
  color: white;
}
.nickname.副社长 .badge {
  background-color: #f1c40f;
  color: white;
}
.nickname.干事 .badge {
  background-color: #3498db;
  color: white;
}

/* 系统消息样式 */
.chat-message.system-message {
  text-align: center;
  margin: 12px 0;
  font-style: italic;
  opacity: 0.7;
  font-size: 13px;
}

.chat-message.system-message .chat-bubble {
  background: transparent;
  padding: 0;
  box-shadow: none;
}

.chat-message.system-message .user-info {
  display: none;
}

.chat-message.system-message .content {
  display: inline;
  font-size: 14px;
}

/* 响应式支持 */
@media screen and (max-width: 768px) {
  .chatroom-wrapper {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid #ddd;
  }

  .chatroom-container {
    padding: 16px;
  }

  .chat-log {
    padding: 12px;
  }
}
</style>


