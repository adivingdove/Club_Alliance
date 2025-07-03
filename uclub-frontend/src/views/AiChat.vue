<template>
  <div class="ai-home-container">
    <div class="ai-sidebar">
      <div class="sidebar-header">
        <span>历史对话</span>
        <button class="new-chat-btn" @click="createNewChat">新建聊天</button>
      </div>
      <ul class="chat-list">
        <li v-for="chat in chatList" :key="chat.id" :class="{active: chat.id === currentChatId}">
          <div class="chat-list-row">
            <span class="chat-title" @click="switchChat(chat.id)">{{ chat.title || ('对话' + chat.id) }}</span>
            <button class="delete-chat-btn" @click.stop="deleteChat(chat.id)">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor" style="vertical-align: middle;"><path d="M6 19a2 2 0 0 0 2 2h8a2 2 0 0 0 2-2V7H6zm3.46-7.12a1 1 0 0 1 1.41 0l1.13 1.13 1.13-1.13a1 1 0 1 1 1.41 1.41l-1.13 1.13 1.13 1.13a1 1 0 1 1-1.41 1.41l-1.13-1.13-1.13 1.13a1 1 0 1 1-1.41-1.41l1.13-1.13-1.13-1.13a1 1 0 0 1 0-1.41z"/><path d="M19 4h-3.5l-1-1h-5l-1 1H5a1 1 0 0 0 0 2h14a1 1 0 0 0 0-2z"/></svg>
            </button>
          </div>
        </li>
      </ul>
    </div>
    <div class="ai-main">
      <div v-if="messages.length > 0" class="ai-messages-scroll">
        <div class="ai-messages">
          <div v-for="msg in messages" :key="msg.id" :class="['ai-message-row', msg.role === '你' ? 'user' : 'ai']">
            <div class="msg-inner">
              <!-- AI 消息：头像和昵称在气泡左上角 -->
              <div v-if="msg.role !== '你'" class="bubble-wrap ai-bubble-wrap">
                <div class="ai-bubble-header">
                  <img class="avatar" src="/vite.svg" alt="AI" />
                  <span class="nickname">万事通</span>
                </div>
                <div class="bubble">
                  <span v-if="msg.id === 'typing'">
                    <span class="dot-flashing"></span>
                    <span style="margin-left:8px;">思考中...</span>
                  </span>
                  <span v-else v-html="msg.displayContent"></span>
                </div>
              </div>
              <!-- 用户消息：气泡在左，头像和昵称在右侧 -->
              <div v-if="msg.role === '你'" class="bubble-wrap user-bubble-wrap">
                <div class="bubble">
                  <span v-if="msg.id === 'typing'">
                    <span class="dot-flashing"></span>
                    <span style="margin-left:8px;">思考中...</span>
                  </span>
                  <span v-else v-html="msg.displayContent"></span>
                </div>
                <div class="user-bubble-header">
                  <img class="avatar" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" alt="你" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="ai-input-bar">
        <input
          v-model="input"
          @keyup.enter="send"
          placeholder="请输入你的问题..."
        />
        <button @click="send">发送</button>
      </div>
    </div>
  </div>
</template>

<script>
import { askAi } from '@/api/aiApi';
import MarkdownIt from 'markdown-it';
import { ElMessageBox } from 'element-plus';
const md = new MarkdownIt();

const CHAT_KEY = 'ai_chat_list_v2';

export default {
  data() {
    return {
      input: '',
      messages: [],
      chatList: [],
      currentChatId: null
    }
  },
  async created() {
    // 登录保护：未登录弹窗提示
    const user = localStorage.getItem('user');
    if (!user) {
      await ElMessageBox.alert('请先登录后再使用AI对话功能', '未登录提示', {
        confirmButtonText: '去登录',
        type: 'warning',
        showClose: false,
        closeOnClickModal: false
      });
      this.$router.replace('/login');
    }
    this.loadChatList();
    if (this.chatList.length > 0) {
      this.switchChat(this.chatList[0].id);
    } else {
      this.createNewChat();
    }
  },
  methods: {
    send() {
      if (!this.input) return;
      const userInput = this.input;
      this.input = '';
      this.messages.push({ role: '你', content: userInput, displayContent: userInput, id: Date.now() });
      this.saveCurrentChat();
      // 插入AI思考中动画
      this.messages.push({ role: 'AI', content: '思考中...', displayContent: '思考中...', id: 'typing' });
      askAi(userInput).then(res => {
        const aiContent = res.data;
        // 移除思考中动画
        this.messages = this.messages.filter(m => m.id !== 'typing');
        const aiMsg = { role: 'AI', content: aiContent, displayContent: '', id: Date.now() + 1 };
        this.messages.push(aiMsg);
        this.typeWriterMarkdown(aiMsg, aiContent);
        this.saveCurrentChat();
      }).catch(e => {
        this.messages = this.messages.filter(m => m.id !== 'typing');
        this.messages.push({ role: 'AI', content: 'AI接口调用失败，请稍后重试。', displayContent: 'AI接口调用失败，请稍后重试。', id: Date.now() + 2 });
        this.saveCurrentChat();
      });
    },
    async typeWriterMarkdown(msg, fullText) {
      msg.displayContent = md.render(fullText);
    },
    sleep(ms) {
      return new Promise(resolve => setTimeout(resolve, ms));
    },
    // 聊天存储相关
    loadChatList() {
      const raw = localStorage.getItem(CHAT_KEY);
      if (raw) {
        this.chatList = JSON.parse(raw);
      } else {
        this.chatList = [];
      }
    },
    saveCurrentChat() {
      if (!this.currentChatId) return;
      const idx = this.chatList.findIndex(c => c.id === this.currentChatId);
      if (idx !== -1) {
        this.chatList[idx].messages = this.messages;
        this.chatList[idx].title = this.getChatTitle(this.messages);
      }
      localStorage.setItem(CHAT_KEY, JSON.stringify(this.chatList));
    },
    createNewChat() {
      const newId = Date.now();
      const newChat = {
        id: newId,
        title: '新对话',
        messages: []
      };
      this.chatList.unshift(newChat);
      this.currentChatId = newId;
      this.messages = [];
      localStorage.setItem(CHAT_KEY, JSON.stringify(this.chatList));
    },
    switchChat(chatId) {
      const chat = this.chatList.find(c => c.id === chatId);
      if (chat) {
        this.currentChatId = chatId;
        this.messages = chat.messages || [];
      }
    },
    getChatTitle(messages) {
      // 取第一条用户消息作为标题
      const first = messages.find(m => m.role === '你');
      return first ? first.content.slice(0, 12) : '新对话';
    },
    async deleteChat(chatId) {
      try {
        await ElMessageBox.confirm('确定要删除该聊天记录吗？', '删除确认', {
          confirmButtonText: '删除',
          cancelButtonText: '取消',
          type: 'warning',
        });
        const idx = this.chatList.findIndex(c => c.id === chatId);
        if (idx !== -1) {
          this.chatList.splice(idx, 1);
          localStorage.setItem(CHAT_KEY, JSON.stringify(this.chatList));
          if (this.currentChatId === chatId) {
            if (this.chatList.length > 0) {
              this.switchChat(this.chatList[0].id);
            } else {
              this.createNewChat();
            }
          }
        }
      } catch (e) {
        // 用户取消，无需处理
      }
    }
  }
}
</script>

<style scoped>
.ai-home-container {
  min-height: 100vh;
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  justify-content: flex-start;
  background: #fafbfc;
  padding-bottom: 0;
}
.ai-sidebar {
  width: 220px;
  background: #fff;
  border-right: 1px solid #e4e7ed;
  min-height: 100vh;
  height: 100vh;
  padding: 18px 0 0 0;
  box-sizing: border-box;
  position: sticky;
  top: 0;
  left: 0;
  overflow-y: auto;
}
.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 18px 12px 18px;
  font-weight: bold;
  font-size: 16px;
}
.new-chat-btn {
  background: #409EFF;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 4px 12px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
}
.new-chat-btn:hover {
  background: #1976d2;
}
.chat-list {
  list-style: none;
  padding: 0 0 0 0;
  margin: 0;
}
.chat-list li {
  padding: 0;
}
.chat-list-row {
  display: flex;
  align-items: center;
  padding: 10px 18px;
}
.chat-title {
  flex: 1 1 auto;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 140px;
  display: inline-block;
  cursor: pointer;
}
.delete-chat-btn {
  background: #f56c6c;
  color: #fff;
  border: none;
  margin-left: 8px;
  font-size: 15px;
  cursor: pointer;
  border-radius: 50%;
  width: 28px;
  height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s, color 0.2s;
  vertical-align: middle;
}
.delete-chat-btn:hover {
  background: #d32f2f;
  color: #fff;
}
.ai-main {
  flex: 1 1 auto;
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  position: relative;
  min-height: 100vh;
  background: #fafbfc;
}
.ai-messages-scroll {
  width: 100%;
  max-width: 1000px;
  margin-bottom: 32px;
  background: transparent;
  padding-bottom: 16px;
  flex: 1 1 auto;
  overflow-y: auto;
  margin-top: 32px;
}
.ai-messages {
  width: 100%;
  max-width: 1000px;
}
.ai-message-row {
  display: flex;
  justify-content: flex-start;
  margin-bottom: 24px;
}
.ai-message-row.user {
  justify-content: flex-end;
}
.msg-inner {
  display: flex;
  align-items: flex-end;
}
.bubble-wrap {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}
.user-bubble-wrap {
  flex-direction: row;
  align-items: flex-end;
}
.user-bubble-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-left: 12px;
  min-width: 60px;
}
.ai-bubble-wrap {
  flex-direction: column;
  align-items: flex-start;
}
.ai-bubble-header {
  display: flex;
  align-items: center;
  margin-bottom: 4px;
}
.ai-bubble-header .avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #f3f4f6;
  margin-right: 6px;
}
.ai-bubble-header .nickname {
  font-size: 20px;
  color: #8b95a1;
}
.user-bubble-header .avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: #f3f4f6;
  margin-top: 6px;
}
.user-bubble-header .nickname {
  font-size: 13px;
  color: #8b95a1;
  margin-bottom: 2px;
}
.bubble {
  max-width: 800px;
  padding: 14px 20px;
  border-radius: 18px;
  font-size: 20px;
  line-height: 1.7;
  word-break: break-word;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  background: #fff;
  color: #222;
}
.ai-message-row.user .bubble {
  background: #1976d2;
  color: #fff;
}
.ai-message-row.ai .bubble {
  background: #fff;
  color: #222;
}
.ai-input-bar {
  width: 100%;
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px 0 24px 0;
  background: #fff;
  display: flex;
  justify-content: center;
  box-shadow: 0 -2px 8px rgba(0,0,0,0.04);
  z-index: 10;
  position: sticky;
  bottom: 0;
}
.ai-input-bar input {
  width: 60%;
  padding: 12px 16px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 18px;
  margin-right: 16px;
  outline: none;
}
.ai-input-bar button {
  padding: 12px 32px;
  background: #1976d2;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 18px;
  cursor: pointer;
  transition: background 0.2s;
}
.ai-input-bar button:hover {
  background: #1565c0;
}
.dot-flashing {
  position: relative;
  width: 10px;
  height: 10px;
  border-radius: 5px;
  background-color: #1976d2;
  color: #1976d2;
  animation: dotFlashing 1s infinite linear alternate;
  display: inline-block;
}
@keyframes dotFlashing {
  0% { opacity: 1; }
  50% { opacity: 0.3; }
  100% { opacity: 1; }
}
</style>