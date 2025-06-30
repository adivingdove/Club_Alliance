<template>
  <div class="post-admin">
    <el-card>
      <el-table :data="postList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="clubName" label="社团" />
        <el-table-column prop="content" label="内容预览">
    
          <template #default="{ row }">
            {{ row.content.slice(0, 30) }}...
          </template>
        
        </el-table-column>
        <el-table-column prop="user_id" label="作者ID" width="100" />
        <el-table-column label="发布时间" width="180">
          <template #default="{ row }">{{ formatDate(row.created_at || row.createdAt) }}</template>
        </el-table-column>

        <el-table-column prop="likeCount" label="点赞" width="80" />
        <el-table-column label="操作" width="100">
            <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewPost(row.id)">查看</el-button>
            </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          background
          layout="prev, pager, next"
          :current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <el-dialog
        title="帖子详情"
        v-model="dialogVisible"
        width="600px"
    >
  <template #default>
    <div v-if="selectedPost">
      <p><strong>标题：</strong>{{ selectedPost.title }}</p>
      <p><strong>社团：</strong>{{ selectedPost.clubName || '暂无' }}</p>
      <p><strong>作者 ID：</strong>{{ selectedPost.user_id }}</p>
      <p><strong>发布时间：</strong>{{ formatDate(selectedPost.created_at) }}</p>
      <p><strong>点赞数：</strong>{{ selectedPost.likeCount }}</p>
      <p><strong>内容：</strong></p>
      <div style="white-space: pre-wrap; padding-left: 1em;">{{ selectedPost.content }}</div>
    </div>
  </template>
</el-dialog>

  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'PostAdminView',
  data() {
  return {
    postList: [],
    currentPage: 1,
    pageSize: 10,
    total: 0,
    dialogVisible: false,       // 控制弹窗是否显示
    selectedPost: null          // 当前选中的帖子内容
  };
},
  

  methods: {
      formatDate(dateStr) {
        if (!dateStr) return ''
        const date = new Date(dateStr)
        return date.toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit'
        })
      },

    async viewPost(id) {
      try {
        const res = await axios.get(`http://localhost:8080/api/admin/posts/${id}`);
        this.selectedPost = res.data;
        this.dialogVisible = true;
      } catch (err) {
        console.error('获取帖子详情失败', err);
      }
    },
    async fetchData(page = 1) {
      try {
        const response = await axios.get('http://localhost:8080/api/admin/posts', {
            params: {
            page: this.currentPage,
            size: this.pageSize,
            title: this.searchTitle || '',
            clubName: this.searchClub || '',
            timeRange: this.searchTimeRange || ''
        }
    });
        this.postList = response.data.content;
        this.total = response.data.totalElements;
        this.currentPage = response.data.number + 1;
      } catch (error) {
        console.error('加载帖子失败', error);
      }
    },
  },
  mounted() {
    this.fetchData();
  },



};
</script>

<style scoped>
.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>



