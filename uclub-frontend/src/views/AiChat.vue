<template>
  <div class="ai-home-container">
    <div class="ai-welcome">
      <div class="ai-title">我是 万事通，很高兴帮助你！</div>
      <div class="ai-subtitle">我可以帮你介绍社团、提供建议，帮助你解决各种问题，请把你的问题交给我吧~</div>
    </div>
    <div class="ai-messages-scroll">
      <div v-if="messages.length > 0" class="ai-messages">
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
</template>

<script>
import { askAi } from '@/api/aiApi';
import MarkdownIt from 'markdown-it';
import { ElMessageBox } from 'element-plus';
const md = new MarkdownIt();

export default {
  data() {
    return {
      input: '',
      messages: []
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
  },
  methods: {
    async send() {
      if (!this.input) return;
      const userInput = this.input;
      this.input = '';
      this.messages.push({ role: '你', content: userInput, displayContent: userInput, id: Date.now() });
      // 插入AI思考中动画
      this.messages.push({ role: 'AI', content: '思考中...', displayContent: '思考中...', id: 'typing' });
      try {
        const res = await askAi(userInput);
        const aiContent = res.data;
        // 移除思考中动画
        this.messages = this.messages.filter(m => m.id !== 'typing');
        const aiMsg = { role: 'AI', content: aiContent, displayContent: '', id: Date.now() + 1 };
        this.messages.push(aiMsg);
        await this.typeWriterMarkdown(aiMsg, aiContent);
      } catch (e) {
        this.messages = this.messages.filter(m => m.id !== 'typing');
        this.messages.push({ role: 'AI', content: 'AI接口调用失败，请稍后重试。', displayContent: 'AI接口调用失败，请稍后重试。', id: Date.now() + 2 });
      }
    },
    async typeWriterMarkdown(msg, fullText) {
      msg.displayContent = md.render(fullText);
    },
    sleep(ms) {
      return new Promise(resolve => setTimeout(resolve, ms));
    }
  }
}
</script>

<style scoped>
.ai-home-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  background: #fafbfc;
  padding-bottom: 120px;
}
.ai-welcome {
  margin-top: 8vh;
  margin-bottom: 48px;
  text-align: center;
}
.ai-title {
  font-size: 2.2rem;
  font-weight: bold;
  color: #2d3a4b;
  margin-bottom: 18px;
}
.ai-subtitle {
  font-size: 1.1rem;
  color: #6b7280;
  margin-bottom: 10px;
}
.ai-messages-scroll {
  flex: 1 1 auto;
  width: 100%;
  max-width: 1000px;
  height: 60vh;
  overflow-y: auto;
  margin-bottom: 32px;
  background: transparent;
  padding-bottom: 16px;
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
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  padding: 24px 0 24px 0;
  display: flex;
  justify-content: center;
  box-shadow: 0 -2px 8px rgba(0,0,0,0.04);
  z-index: 10;
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