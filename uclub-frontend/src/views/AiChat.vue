<template>
  <div class="chat-container">
    <div v-for="msg in messages" :key="msg.id" :class="['chat-message', msg.role === '你' ? 'user' : 'ai']">
      <div class="bubble">
        <span v-if="msg.role !== '你'" style="font-weight:bold;">万事通：</span>
        <span v-if="msg.id === 'typing'">
          <span class="dot-flashing"></span>
          <span style="margin-left:8px;">思考中...</span>
        </span>
        <span v-else v-html="msg.displayContent"></span>
      </div>
    </div>
    <div class="input-bar">
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
const md = new MarkdownIt();

export default {
  data() {
    return {
      input: '',
      messages: []
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
      // 最终确保完整渲染
      msg.displayContent = md.render(fullText);
    },
    sleep(ms) {
      return new Promise(resolve => setTimeout(resolve, ms));
    }
  }
}
</script>

<style scoped>
.chat-container {
  max-width: 600px;
  margin: 40px auto;
  background: #f7f7f7;
  border-radius: 12px;
  padding: 24px 16px 80px 16px;
  min-height: 60vh;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  position: relative;
}
.chat-message {
  display: flex;
  margin-bottom: 16px;
}
.chat-message.user {
  justify-content: flex-end;
}
.chat-message.ai {
  justify-content: flex-start;
}
.bubble {
  max-width: 70%;
  padding: 12px 18px;
  border-radius: 18px;
  font-size: 16px;
  line-height: 1.6;
  word-break: break-word;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}
.user .bubble {
  background: #1976d2;
  color: #fff;
  border-bottom-right-radius: 6px;
  border-bottom-left-radius: 18px;
  border-top-left-radius: 18px;
  border-top-right-radius: 18px;
}
.ai .bubble {
  background: #fff;
  color: #222;
  border-bottom-left-radius: 6px;
  border-bottom-right-radius: 18px;
  border-top-left-radius: 18px;
  border-top-right-radius: 18px;
}
.input-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  padding: 16px 0 16px 0;
  display: flex;
  justify-content: center;
  box-shadow: 0 -2px 8px rgba(0,0,0,0.04);
}
.input-bar input {
  width: 60%;
  padding: 10px 14px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 16px;
  margin-right: 12px;
  outline: none;
}
.input-bar button {
  padding: 10px 24px;
  background: #1976d2;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.2s;
}
.input-bar button:hover {
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