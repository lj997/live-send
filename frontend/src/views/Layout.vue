<template>
  <el-container class="layout-container">
    <el-aside width="240px" class="sidebar">
      <div class="logo">
        <el-icon :size="32" color="#FF6B9D"><HeartFilled /></el-icon>
        <span class="logo-text">Live Send</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        background-color="transparent"
        text-color="#666"
        active-text-color="#FF6B9D"
        router
        class="sidebar-menu"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/files">
          <el-icon><Folder /></el-icon>
          <span>文件管理</span>
        </el-menu-item>
        <el-menu-item index="/notes">
          <el-icon><Notebook /></el-icon>
          <span>备忘录</span>
        </el-menu-item>
        <el-menu-item index="/contacts">
          <el-icon><User /></el-icon>
          <span>联系人</span>
        </el-menu-item>
        <el-menu-item index="/tasks">
          <el-icon><Clock /></el-icon>
          <span>发送任务</span>
        </el-menu-item>
        <el-menu-item index="/checkin">
          <el-icon><CircleCheck /></el-icon>
          <span>签到</span>
        </el-menu-item>
        <el-menu-item index="/settings">
          <el-icon><Setting /></el-icon>
          <span>设置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-main class="main-content">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #FFF5F5 0%, #FFE4E9 100%);
}

.sidebar {
  background-color: white;
  box-shadow: 2px 0 10px rgba(255, 107, 157, 0.1);
  height: 100vh;
  position: fixed;
  left: 0;
  top: 0;
  overflow-y: auto;
}

.logo {
  display: flex;
  align-items: center;
  padding: 24px 20px;
  border-bottom: 1px solid var(--border-color);
  margin-bottom: 12px;
}

.logo-text {
  font-size: 20px;
  font-weight: 600;
  color: var(--primary-color);
  margin-left: 12px;
}

.sidebar-menu {
  border-right: none;
}

.sidebar-menu .el-menu-item {
  margin: 4px 12px;
  border-radius: 8px;
  height: 48px;
  line-height: 48px;
}

.sidebar-menu .el-menu-item:hover {
  background-color: rgba(255, 107, 157, 0.08);
}

.sidebar-menu .el-menu-item.is-active {
  background: linear-gradient(135deg, rgba(255, 107, 157, 0.15) 0%, rgba(255, 182, 193, 0.15) 100%);
}

.main-content {
  margin-left: 240px;
  padding: 24px;
  min-height: 100vh;
}
</style>
