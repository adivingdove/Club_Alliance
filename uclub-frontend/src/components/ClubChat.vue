<template>
  <div class="club-chat-flex">
    <aside class="online-users">
      <div class="online-title">在线成员</div>
      <div class="online-divider"></div>
      <div v-if="onlineUsers.length === 0" class="online-empty">暂无成员在线</div>
      <div v-for="user in onlineUsers" :key="user.id" class="online-user">
        <el-avatar :src="formatAvatar(user.avatar)" :size="36" shape="circle" />
        <span class="online-nickname">{{ user.nickname }}</span>
      </div>
    </aside>
    <section class="club-chat-wrapper">
      <div class="chat-header">
        <el-icon style="margin-right:8px;"><i class="el-icon-message" /></el-icon>
        <span class="chat-room-title">社团聊天室</span>
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
              <el-avatar :src="formatAvatar(msg.avatar)" :size="30" shape="circle" />
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
        <el-input ref="inputRef" v-model="message" @keyup.enter="sendMessage" placeholder="说点什么..." class="chat-input-box" />
        <el-button type="primary" @click="sendMessage" class="chat-send-btn">发送</el-button>
        <el-button @click="showEmoji = !showEmoji" class="chat-emoji-btn">😊</el-button>
      </div>
      <div v-if="showEmoji" class="emoji-picker-wrapper">
        <emoji-picker @emoji-click="addEmoji" />
      </div>
    </section>
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
  if (!stompClient || !stompClient.connected) {
    console.error('[ClubChat] stompClient 未连接，无法发送消息')
    return
  }
  const msg = {
    sender: currentUser.value.nickname,
    avatar: currentUser.value.headUrl,
    content: message.value,
    time: new Date().toISOString(),
    role: currentUser.value.role || '成员',
    room: roomId.value
  }
  stompClient.publish({
    destination: `/app/chat.send.club-${props.clubId}`,
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
.club-chat-flex {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  gap: 0;
  background: #f7f8fa;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  min-height: 900px;
}
.online-users {
  min-width: 200px;
  max-width: 240px;
  background: #fff;
  border-radius: 16px 0 0 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  padding: 48px 18px 48px 18px;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}
.online-title {
  font-size: 18px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 8px;
}
.online-divider {
  width: 100%;
  height: 1px;
  background: #f0f0f0;
  margin-bottom: 12px;
}
.online-empty {
  color: #bbb;
  font-size: 14px;
  margin: 16px 0;
}
.online-user {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 0;
  padding: 6px 8px;
  border-radius: 12px;
  transition: background 0.2s;
  cursor: pointer;
  border: 2px solid #222;
  box-sizing: border-box;
}
.online-user:hover {
  background: #f5f7fa;
}
.online-nickname {
  font-size: 15px;
  color: #333;
  font-weight: 500;
}
.club-chat-wrapper {
  flex: 1;
  background: #fff;
  border-radius: 0 16px 16px 0;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  padding: 64px 64px 40px 64px;
  margin: 0;
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.chat-header {
  display: flex;
  align-items: center;
  font-size: 22px;
  font-weight: bold;
  color: #222;
  margin-bottom: 18px;
}
.chat-room-title {
  font-size: 22px;
  font-weight: bold;
  color: #409EFF;
}
.chat-log {
  min-height: 600px;
  max-height: 760px;
  overflow-y: auto;
  margin-bottom: 16px;
  padding-right: 8px;
}
.chat-message {
  margin-bottom: 14px;
  display: flex;
  flex-direction: row;
}
.system-message .chat-bubble {
  background: #f0f0f0;
  color: #888;
  text-align: center;
  border-radius: 12px;
  box-shadow: none;
}
.my-message .chat-bubble {
  background: linear-gradient(90deg, #aee2ff 0%, #e0f7fa 100%);
  text-align: right;
  border-radius: 16px 16px 4px 16px;
  box-shadow: 0 2px 8px rgba(64,158,255,0.08);
  margin-left: auto;
}
.other-message .chat-bubble {
  background: linear-gradient(90deg, #f8e1ff 0%, #f7f8fa 100%);
  border-radius: 16px 16px 16px 4px;
  box-shadow: 0 2px 8px rgba(255,192,203,0.08);
  margin-right: auto;
}
.chat-bubble {
  display: inline-block;
  padding: 12px 18px;
  border-radius: 16px;
  max-width: 80%;
  word-break: break-all;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  background: #f7f8fa;
  position: relative;
}
.user-info {
  display: flex;
  align-items: center;
  margin-bottom: 4px;
}
.nickname {
  font-weight: bold;
  margin-left: 8px;
  font-size: 15px;
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
  text-align: right;
}
.chat-inputs {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
  background: #f7f8fa;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(64,158,255,0.04);
  padding: 10px 14px;
}
.chat-input-box {
  flex: 1;
  border-radius: 8px;
  font-size: 15px;
  background: #fff;
  border: 1px solid #e0e0e0;
  box-shadow: none;
}
.chat-send-btn {
  border-radius: 8px;
  font-weight: bold;
  font-size: 15px;
  background: linear-gradient(90deg, #409EFF 0%, #79bbff 100%);
  color: #fff;
  border: none;
  box-shadow: 0 2px 8px rgba(64,158,255,0.08);
  transition: background 0.2s;
}
.chat-send-btn:hover {
  background: linear-gradient(90deg, #66b1ff 0%, #409EFF 100%);
}
.chat-emoji-btn {
  border-radius: 8px;
  font-size: 18px;
  background: #fff;
  border: 1px solid #e0e0e0;
  color: #faad14;
  box-shadow: none;
}
.emoji-picker-wrapper {
  margin-bottom: 8px;
}
@media (max-width: 900px) {
  .club-chat-flex {
    flex-direction: column;
  }
  .online-users {
    max-width: 100%;
    min-width: 0;
    flex-direction: row;
    flex-wrap: wrap;
    border-radius: 16px 16px 0 0;
    margin: 0 0 12px 0;
    padding: 16px 8px;
  }
  .club-chat-wrapper {
    border-radius: 0 0 16px 16px;
    padding: 18px 8px 12px 8px;
  }
}
</style> 