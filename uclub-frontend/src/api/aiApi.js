import request from '../utils/request';

export function askAi(question) {
  return request.post('/api/ai/ask', { question });
} 