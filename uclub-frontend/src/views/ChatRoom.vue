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
  align-items: stretch;
  min-height: calc(100vh - 64px);
  max-width: 1050px;
  margin: 16px auto 16px auto;
  background: linear-gradient(to right, #f0faff, #e8f5ff);
  font-family: 'Segoe UI', sans-serif;
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(161,140,209,0.08);
  padding-top: 0;
}

.sidebar {
  width: 240px;
  background: #fff;
  border-right: 1.5px solid #f3eaff;
  padding: 32px 18px 24px 18px;
  box-shadow: 2px 0 12px rgba(161,140,209,0.04);
  border-radius: 20px 0 0 20px;
}
.sidebar h3 {
  font-size: 18px;
  margin-bottom: 20px;
  color: #a18cd1;
}

.online-users {
  margin-top: 32px;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 2px 8px rgba(161,140,209,0.06);
  padding: 18px 12px 12px 12px;
  border: 1.5px solid #f3eaff;
}
.online-users h3 {
  font-size: 16px;
  color: #a18cd1;
  margin-bottom: 12px;
  font-weight: 600;
}
.online-user {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;
  border-radius: 8px;
  transition: background-color 0.2s ease;
  font-size: 15px;
  color: #444;
}
.online-user:hover {
  background-color: #f7f8fa;
  cursor: pointer;
}
.online-users > div[style*='暂无成员在线'] {
  color: #bbb;
  font-size: 14px;
  text-align: center;
  padding: 12px 0;
}

.chatroom-menu {
  border-right: none;
}

.chatroom-menu .el-menu-item {
  padding-left: 16px !important;
  border-radius: 8px;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #888;
  font-weight: 500;
}
.chatroom-menu .el-menu-item:hover {
  background: #f7f8fa;
  color: #a18cd1;
}
.chatroom-menu .el-menu-item.is-active {
  background: linear-gradient(90deg, #a18cd1 0%, #fbc2eb 100%);
  color: #fff;
  font-weight: bold;
}

.dot {
  background-color: #ff4d4f;
}

.chatroom-container {
  flex: 1;
  padding: 24px 24px 40px 24px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
@media (max-width: 1200px) {
  .chatroom-container {
    padding: 16px 10px 28px 10px;
  }
}
@media (max-width: 768px) {
  .chatroom-wrapper {
    align-items: stretch;
  }
  .chatroom-container {
    padding: 6px 2vw 18px 2vw;
  }
}

.chatroom-container h2 {
  margin-bottom: 15px;
  font-size: 22px;
  color: #a18cd1;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 6px;
  background: #fff;
  padding: 10px 16px;
  border-radius: 14px;
  box-shadow: 0 2px 8px rgba(161,140,209,0.06);
  border: 1.5px solid #f3eaff;
}

.chat-log {
  flex: 1;
  overflow-y: auto;
  padding: 16px 16px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 18px rgba(161,140,209,0.08);
  margin-bottom: 16px;
  border: 1.5px solid #f3eaff;
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
  background: #f7f8fa;
  padding: 14px 20px;
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(161,140,209,0.06);
  word-break: break-word;
  transition: all 0.2s ease-in-out;
  border: 1.5px solid #e0c3fc;
}
.my-message .chat-bubble {
  background: #e6f0ff; 
  color: #333;
  text-align: right;
  border-top-right-radius: 8px;
  border: 1.5px solid #c8ddff;
}

.other-message .chat-bubble {
  background: #f5f7fa; 
  border: 1.5px solid #e0e0e0;
  color: #333;
  border-top-left-radius: 8px;
}

.user-info .nickname {
  font-weight: 600;
  font-size: 15px;
  color: #a18cd1;
}

.content {
  margin-bottom: 6px;
  font-size: 15px;
  line-height: 1.5;
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
  margin-top: 6px;
  background: #fff;
  border-top: 1.5px solid #f3eaff;
  padding: 10px 18px;
  border-radius: 0 0 16px 16px;
  box-shadow: 0 -2px 8px rgba(161,140,209,0.06);
}

.el-input {
  font-size: 15px;
  border-radius: 10px;
  border: 1.5px solid #e0c3fc;
  background: #fafbfc;
}

.el-button[type="primary"] {
  background: linear-gradient(90deg, #a18cd1 0%, #fbc2eb 100%);
  color: #fff;
  border: none;
  font-weight: bold;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(161,140,209,0.06);
  transition: background 0.2s, color 0.2s;
}
.el-button[type="primary"]:hover {
  background: linear-gradient(90deg, #fbc2eb 0%, #a18cd1 100%);
  color: #fff;
}
.el-button {
  border-radius: 10px;
  color: #a18cd1;
  border: 1.5px solid #a18cd1;
  background: #fff;
  font-weight: bold;
  transition: background 0.2s, color 0.2s;
}
.el-button:hover {
  background: #a18cd1;
  color: #fff;
}

.emoji-picker-wrapper {
  margin-top: 12px;
  border: 1.5px solid #e0c3fc;
  border-radius: 12px;
  max-width: 320px;
  background: #fff;
  box-shadow: 0 4px 16px rgba(161,140,209,0.13);
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
@media screen and (max-width: 1200px) {
  .chatroom-wrapper {
    max-width: 98vw;
    border-radius: 12px;
  }
  .sidebar {
    border-radius: 12px 12px 0 0;
  }
}
@media screen and (max-width: 768px) {
  .chatroom-wrapper {
    flex-direction: column;
    min-height: auto;
    margin: 0 auto 0 auto;
    border-radius: 0;
    box-shadow: none;
  }
  .sidebar {
    width: 100%;
    border-right: none;
    border-bottom: 1.5px solid #f3eaff;
    padding: 12px 4vw 8px 4vw;
    border-radius: 0 0 0 0;
  }
  .online-users {
    border-radius: 10px;
    padding: 10px 6px 6px 6px;
  }
  .chatroom-container {
    padding: 4px 2vw 2px 2vw;
  }
  .chat-log {
    padding: 8px 2px;
    border-radius: 10px;
    margin-bottom: 8px;
  }
  .chat-inputs {
    margin-top: 4px;
    padding: 6px 8px;
  }
}
</style>


