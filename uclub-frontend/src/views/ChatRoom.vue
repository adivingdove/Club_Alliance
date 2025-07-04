<template>
  <div class="chatroom-wrapper">
    <!-- 左侧侧边栏：聊天室列表 + 在线成员 -->
    <div class="sidebar">
      <h3>聊天室列表</h3>
      <el-menu class="chatroom-menu" :default-active="currentRoom" @select="switchRoom">
        <!-- 通用聊天室 -->
        <el-menu-item index="public">
          🌐 通用聊天室
          <span v-if="unreadMap['public']" class="dot"></span>
        </el-menu-item>
        <!-- 社团聊天室 -->
        <el-menu-item
          v-for="club in myClubs"
          :key="club.id"
          :index="'club-' + club.id"
        >
          🏷 {{ club.name }}
          <span v-if="unreadMap['club-' + club.id]" class="dot"></span>
        </el-menu-item>
      </el-menu>

      <div class="online-users">
        <h3>在线成员</h3>
        <div v-if="onlineUsers.length === 0">暂无成员在线</div>
        <div v-for="user in onlineUsers" :key="user.id" class="online-user">
          <el-avatar :src="formatAvatar(user.avatar)" :size="24" />
          <span>{{ user.nickname }}</span>
        </div>
      </div>
    </div>

    <!-- 聊天内容区域 -->
    <div class="chatroom-container">
      <h2>
  {{ currentRoomLabel }}
<el-button
  size="small"
  type="text"
  style="margin-left: auto; font-size: 13px;"
  @click="toggleHistory"
>
  {{ showHistory ? ' 收起历史消息' : ' 查看历史消息' }}
</el-button>

</h2>
  <div v-if="showHistory" class="chat-history">
  <h4 class="history-title">📜 历史消息</h4>

  <div
    v-for="(msg, idx) in historyMessages"
    :key="'history-' + idx"
    class="chat-message other-message"
  >
    <div class="chat-bubble">
      <div class="user-info">
        <el-avatar :src="formatAvatar(msg.avatar)" :size="28" />
        <span class="nickname" :class="msg.role">
          {{ msg.sender }}
          <span v-if="msg.role !== '成员'" class="badge">{{ roleMap[msg.role] }}</span>
        </span>
      </div>
      <div class="content" v-html="formatMessage(msg.content)"></div>
      <div class="time">{{ formatTime(msg.time) }}</div>
    </div>
  </div>
</div>

      <div class="chat-log">
        <div
          v-for="(msg, idx) in messages"
          :key="idx"
          :class="['chat-message',
            msg.role === '系统' ? 'system-message' :
            msg.sender === currentUser.nickname ? 'my-message' : 'other-message']"
        >
          <div class="chat-bubble">
            <div v-if="msg.role !== '系统'" class="user-info">
              <el-avatar :src="formatAvatar(msg.avatar)" :size="30" />
              <span class="nickname" :class="msg.role">
                {{ msg.sender }}
                <span v-if="msg.role !== '成员'" class="badge">{{ roleMap[msg.role] }}</span>
              </span>
            </div>
            <div class="content" v-html="formatMessage(msg.content)"></div>
            <div class="time">{{ formatTime(msg.time) }}</div>
          </div>
        </div>
      </div>

      <div class="chat-inputs">
        <el-input ref="inputRef" v-model="message" @keyup.enter="sendMessage" placeholder="说点什么..." />
        <el-button type="primary" @click="sendMessage">发送</el-button>
        <el-button @click="showEmoji = !showEmoji">😊</el-button>
      </div>

      <div v-if="showEmoji" class="emoji-picker-wrapper">
        <emoji-picker @emoji-click="addEmoji" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick, watchEffect } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client/dist/sockjs.min.js'
import { getMyClubs, getProfileInfo } from '@/api/profileApi'
import 'emoji-picker-element'
import axios from 'axios'

// 当前状态
const currentUser = ref({})
const currentRoom = ref('public')
const myClubs = ref([])
const onlineUsers = ref([])
const messages = ref([])
const message = ref('')
const inputRef = ref(null)
const showEmoji = ref(false)
const unreadMap = reactive({ public: false })

const apiBaseUrl = 'http://localhost:8080'
const defaultAvatar = 'https://cdn-icons-png.flaticon.com/512/149/149071.png'
const historyMessages = ref([])
const showHistory = ref(false)

// 用户角色徽章
const roleMap = {
  '社长': '👑 社长',
  '副社长': '⭐ 副社长',
  '干事': '🛠 干事',
  '成员': ''
}

let stompClient = null
let intervalId = null

// 聊天室名称标签
const currentRoomLabel = computed(() =>
  currentRoom.value === 'public'
    ? '🟢 通用聊天室'
    : '🟢 ' + (myClubs.value.find(c => 'club-' + c.id === currentRoom.value)?.name || '')
)

// 获取用户信息
const fetchCurrentUser = async () => {
  const res = await getProfileInfo()
  currentUser.value = res.data.data
}

// 获取社团信息
const fetchMyClubs = async () => {
  const res = await getMyClubs()
  myClubs.value = res.data.data.map(c => ({ id: c.id, name: c.name }))
  myClubs.value.forEach(c => unreadMap['club-' + c.id] = false)
}

// 获取在线用户
const fetchOnlineUsers = async () => {
  try {
    const res = await axios.get(`${apiBaseUrl}/api/chat/online/${currentRoom.value}`, {
      headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    })
    onlineUsers.value = res.data
  } catch (err) {
    console.error('获取在线用户失败:', err)
  }
}

// 格式化函数
const formatTime = (iso) => {
  const d = new Date(iso)
  return `${d.getFullYear()}-${(d.getMonth()+1).toString().padStart(2,'0')}-${d.getDate().toString().padStart(2,'0')} ${d.getHours().toString().padStart(2,'0')}:${d.getMinutes().toString().padStart(2,'0')}`
}
const formatMessage = (text) => text.replace(/\n/g, '<br>')
const formatAvatar = (url) => url?.startsWith('http') ? url : (apiBaseUrl + url) || defaultAvatar

// 接收消息回调
const handleIncomingMessage = (msg, roomId) => {
  const parsed = JSON.parse(msg.body)
  if (roomId === currentRoom.value) {
    messages.value.push(parsed)
  } else {
    unreadMap[roomId] = true
    console.log(`[📩 未读消息] 来自房间 ${roomId}`, parsed)
  }
}

// 连接 WebSocket
const connect = () => {
  const socket = new SockJS(`${apiBaseUrl}/ws-chat?token=${localStorage.getItem('token')}`)
  stompClient = new Client({
    webSocketFactory: () => socket,
    connectHeaders: { token: localStorage.getItem('token') },
    onConnect: () => {
      console.log('[ 已连接 WebSocket]')
      subscribeAllRooms()
      subscribeToRoom(currentRoom.value)
      fetchOnlineUsers()
    }
  })
  stompClient.activate()
}

// 订阅所有房间
const subscribeAllRooms = () => {
  stompClient.subscribe('/topic/public', (msg) => handleIncomingMessage(msg, 'public'))
  myClubs.value.forEach(club => {
    const roomId = 'club-' + club.id
    stompClient.subscribe(`/topic/${roomId}`, (msg) => handleIncomingMessage(msg, roomId))
  })
}

// 切换房间
const subscribeToRoom = (roomId) => {
  messages.value = [{
    sender: '系统',
    content: `🎉 欢迎来到「${currentRoomLabel.value.replace('🟢 ', '')}」聊天室！`,
    time: new Date().toISOString(),
    role: '系统'
  }]
  unreadMap[roomId] = false
}
const switchRoom = (roomId) => {
  messages.value.push({
    sender: '系统',
    content: `🚪 离开「${currentRoomLabel.value.replace('🟢 ', '')}」聊天室`,
    time: new Date().toISOString(),
    role: '系统'
  })
  currentRoom.value = roomId
  subscribeToRoom(roomId)
  fetchOnlineUsers()
}

// 发送消息
const sendMessage = () => {
  if (!message.value.trim()) return
 const payload = {
  sender: currentUser.value.nickname,
  avatar: currentUser.value.headUrl || '',
  content: message.value,
  role: '',
  room: currentRoom.value,
  time: new Date().toISOString()
}

  stompClient.publish({
    destination: `/app/chat.send.${currentRoom.value}`,
    body: JSON.stringify(payload),
    headers: { token: localStorage.getItem('token') }
  })
  message.value = ''
}

// 插入 emoji
const addEmoji = (e) => {
  const emoji = e.detail.unicode
  const inputEl = inputRef.value?.input
  const start = inputEl?.selectionStart || 0
  const end = inputEl?.selectionEnd || 0
  message.value = message.value.slice(0, start) + emoji + message.value.slice(end)
  nextTick(() => {
    inputEl.selectionStart = inputEl.selectionEnd = start + emoji.length
    inputEl.focus()
  })
}

const loadHistory = async () => {
  try {
    const res = await axios.get(`${apiBaseUrl}/api/chat/history/${currentRoom.value}`, {
      params: {
        page: 0,
        size: 30
      },
      headers: {
        Authorization: `Bearer ${localStorage.getItem('token')}`
      }
    })
    historyMessages.value = res.data.reverse()
  } catch (err) {
    console.error('加载历史消息失败:', err)
  }
}
const toggleHistory = async () => {
  showHistory.value = !showHistory.value
  if (showHistory.value && historyMessages.value.length === 0) {
    await loadHistory()
  }
}


// 生命周期
onMounted(async () => {
  await fetchCurrentUser()
  await fetchMyClubs()
  connect()
  intervalId = setInterval(fetchOnlineUsers, 10000)
})
onUnmounted(() => {
  stompClient?.deactivate()
  clearInterval(intervalId)
})

// 调试 unreadMap
watchEffect(() => {
  console.log('unreadMap 状态更新:', JSON.stringify(unreadMap))
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
.chat-history {
  margin-bottom: 20px;
  background-color: #f9f9f9;
  padding: 12px;
  border-radius: 10px;
  box-shadow: inset 0 0 4px rgba(0, 0, 0, 0.03);
  max-height: 240px;
  overflow-y: auto;
}

.history-title {
  margin-bottom: 8px;
  color: #888;
  font-size: 13px;
  font-weight: 500;
  padding-left: 4px;
}



.chat-history::-webkit-scrollbar {
  width: 6px;
}
.chat-history::-webkit-scrollbar-thumb {
  background-color: #ccc;
  border-radius: 3px;
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


