<template>
  <div>
    <div style="color:red;font-weight:bold;">ClubChat loaded, clubId: {{ clubId }}</div>
    <div class="club-chat-wrapper">
      <h3>社团聊天室</h3>
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
      <div class="online-users">
        <h4>在线成员</h4>
        <div v-if="onlineUsers.length === 0">暂无成员在线</div>
        <div v-for="user in onlineUsers" :key="user.id" class="online-user">
          <el-avatar :src="formatAvatar(user.avatar)" :size="24" />
          <span>{{ user.nickname }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client/dist/sockjs.min.js'
import { getProfileInfo } from '@/api/profileApi'
import 'emoji-picker-element'
import axios from 'axios'

const props = defineProps({ clubId: { type: [String, Number], required: true } })

console.log('ClubChat loaded, clubId:', props.clubId)

const currentUser = ref({})
const messages = ref([])
const message = ref('')
const inputRef = ref(null)
const showEmoji = ref(false)
const onlineUsers = ref([])
const apiBaseUrl = 'http://localhost:8080'
const defaultAvatar = 'https://cdn-icons-png.flaticon.com/512/149/149071.png'
const roleMap = {
  '社长': '👑 社长',
  '副社长': '⭐ 副社长',
  '干事': '🛠 干事',
  '成员': ''
}
let stompClient = null

const roomId = computed(() => `club-${props.clubId}`)

const formatTime = (iso) => {
  const d = new Date(iso)
  return `${d.getFullYear()}-${(d.getMonth()+1).toString().padStart(2,'0')}-${d.getDate().toString().padStart(2,'0')} ${d.getHours().toString().padStart(2,'0')}:${d.getMinutes().toString().padStart(2,'0')}`
}
const formatMessage = (text) => text.replace(/\n/g, '<br>')
const formatAvatar = (url) => url?.startsWith('http') ? url : (apiBaseUrl + url) || defaultAvatar

const handleIncomingMessage = (msg) => {
  const parsed = JSON.parse(msg.body)
  messages.value.push(parsed)
}

const fetchCurrentUser = async () => {
  const res = await getProfileInfo()
  currentUser.value = res.data.data
}

const fetchOnlineUsers = async () => {
  try {
    const res = await axios.get(`${apiBaseUrl}/api/chat/online/${roomId.value}`, {
      headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    })
    onlineUsers.value = res.data
  } catch (err) {
    console.error('获取在线用户失败:', err)
  }
}

const connect = () => {
  const socket = new SockJS(`${apiBaseUrl}/ws-chat?token=${localStorage.getItem('token')}`)
  stompClient = new Client({
    webSocketFactory: () => socket,
    connectHeaders: { token: localStorage.getItem('token') },
    onConnect: () => {
      console.log('[已连接社团聊天室 WebSocket]')
      stompClient.subscribe(`/topic/${roomId.value}`, handleIncomingMessage)
      fetchOnlineUsers()
      // 系统欢迎消息
      messages.value = [{
        sender: '系统',
        content: `🎉 欢迎来到本社团聊天室！`,
        time: new Date().toISOString(),
        role: '系统'
      }]
    }
  })
  stompClient.activate()
}

const sendMessage = () => {
  if (!message.value.trim()) return
  const msg = {
    sender: currentUser.value.nickname,
    avatar: currentUser.value.headUrl,
    content: message.value,
    time: new Date().toISOString(),
    role: currentUser.value.role || '成员',
    room: roomId.value
  }
  stompClient.publish({
    destination: `/app/chat.send/${roomId.value}`,
    body: JSON.stringify(msg)
  })
  message.value = ''
  nextTick(() => inputRef.value?.focus())
}

const addEmoji = (e) => {
  message.value += e.detail.unicode
  showEmoji.value = false
  nextTick(() => inputRef.value?.focus())
}

onMounted(async () => {
  await fetchCurrentUser()
  connect()
})

onUnmounted(() => {
  if (stompClient) stompClient.deactivate()
})

watch(() => props.clubId, (newId, oldId) => {
  if (stompClient) stompClient.deactivate()
  messages.value = []
  onlineUsers.value = []
  connect()
})
</script>

<style scoped>
.club-chat-wrapper {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  padding: 24px;
  margin: 24px 0;
}
.chat-log {
  min-height: 220px;
  max-height: 320px;
  overflow-y: auto;
  margin-bottom: 16px;
}
.chat-message {
  margin-bottom: 10px;
}
.system-message .chat-bubble {
  background: #f0f0f0;
  color: #888;
  text-align: center;
}
.my-message .chat-bubble {
  background: #e6f7ff;
  text-align: right;
}
.other-message .chat-bubble {
  background: #f9f9f9;
}
.chat-bubble {
  display: inline-block;
  padding: 10px 16px;
  border-radius: 8px;
  max-width: 80%;
  word-break: break-all;
}
.user-info {
  display: flex;
  align-items: center;
  margin-bottom: 4px;
}
.nickname {
  font-weight: bold;
  margin-left: 8px;
}
.badge {
  margin-left: 4px;
  font-size: 0.9em;
  color: #faad14;
}
.time {
  font-size: 0.8em;
  color: #aaa;
  margin-top: 2px;
}
.chat-inputs {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.emoji-picker-wrapper {
  margin-bottom: 8px;
}
.online-users {
  margin-top: 16px;
}
.online-user {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}
</style> 