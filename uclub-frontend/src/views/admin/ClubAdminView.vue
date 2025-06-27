<template>
  <div>
    <h2>社团管理员管理</h2>
    <el-table :data="clubAdmins" style="width: 100%">
      <el-table-column prop="clubName" label="社团名称" width="200" />
      <el-table-column label="管理员列表">
        <template #default="{ row }">
          <el-tag
            v-for="admin in row.admins"
            :key="admin.userId"
            closable
            @close="handleRevoke(admin.userId)"
            style="margin-right: 5px"
          >
            {{ admin.nickname }} ({{ admin.email }})
          </el-tag>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";

const clubAdmins = ref([]);

async function fetchClubAdmins() {
  try {
    const res = await fetch("http://localhost:8080/api/admin/club-admin/list");
    if (!res.ok) throw new Error("加载失败");
    clubAdmins.value = await res.json();
  } catch (error) {
    ElMessage.error(error.message);
  }
}

async function handleRevoke(userId) {
  try {
    await ElMessageBox.confirm("确定撤销该管理员身份吗？", "警告", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });

    const res = await fetch(`http://localhost:8080/api/admin/club-admin/revoke/${userId}`, {
      method: "POST",
    });

    if (!res.ok) throw new Error("撤销失败");
    ElMessage.success("撤销成功");
    fetchClubAdmins(); // 刷新列表
  } catch (error) {
    if (error !== "取消") ElMessage.error(error.message || "操作失败");
  }
}

onMounted(() => {
  fetchClubAdmins();
});
</script>

<style scoped>
</style>
