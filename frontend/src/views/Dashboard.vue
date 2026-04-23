<template>
  <div>
    <div class="welcome-header">
      <div class="heart-icon">💝</div>
      <h1>欢迎回来</h1>
      <p>传递爱与记忆，让温暖永远留存</p>
    </div>

    <el-row :gutter="24" style="margin-bottom: 24px;">
      <el-col :span="6">
        <div class="card stat-card">
          <div class="stat-number">{{ stats.files }}</div>
          <div class="stat-label">上传文件</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="card stat-card">
          <div class="stat-number">{{ stats.contacts }}</div>
          <div class="stat-label">联系人</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="card stat-card">
          <div class="stat-number">{{ stats.tasks }}</div>
          <div class="stat-label">发送任务</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="card stat-card">
          <div class="stat-number">{{ stats.activeTasks }}</div>
          <div class="stat-label">进行中</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="24">
      <el-col :span="14">
        <div class="card">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
            <h3 style="color: var(--primary-color); font-size: 18px; margin: 0;">
              <el-icon style="margin-right: 8px;"><Clock /></el-icon>
              进行中的任务
            </h3>
            <el-button type="primary" text @click="$router.push('/tasks')">查看全部</el-button>
          </div>
          <el-empty v-if="activeTasks.length === 0" description="暂无进行中的任务" />
          <div v-else>
            <div v-for="task in activeTasks" :key="task.id" class="task-item">
              <div class="task-info">
                <div class="task-name">{{ task.name }}</div>
                <div class="task-detail">
                  <span style="margin-right: 16px;">
                    <el-icon><Folder /></el-icon> {{ task.files.length }} 个文件
                  </span>
                  <span style="margin-right: 16px;">
                    <el-icon><User /></el-icon> {{ task.contacts.length }} 位联系人
                  </span>
                  <span>
                    签到: {{ task.currentCheckIns }}/{{ task.requiredCheckIns }}
                  </span>
                </div>
              </div>
              <div class="task-countdown">
                <div class="countdown-text">剩余</div>
                <div class="countdown-days">{{ remainingDays(task) }}</div>
                <div class="countdown-unit">天</div>
              </div>
            </div>
          </div>
        </div>
      </el-col>

      <el-col :span="10">
        <div class="card">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
            <h3 style="color: var(--primary-color); font-size: 18px; margin: 0;">
              <el-icon style="margin-right: 8px;"><Notebook /></el-icon>
              最近备忘录
            </h3>
            <el-button type="primary" text @click="$router.push('/notes')">查看全部</el-button>
          </div>
          <el-empty v-if="recentNotes.length === 0" description="暂无备忘录" />
          <div v-else>
            <div v-for="note in recentNotes" :key="note.id" class="note-item">
              <div class="note-title">{{ note.title }}</div>
              <div class="note-preview">{{ note.content || '暂无内容' }}</div>
            </div>
          </div>
        </div>

        <div class="card" style="margin-top: 24px;">
          <h3 style="color: var(--primary-color); font-size: 18px; margin: 0 0 20px 0;">
            <el-icon style="margin-right: 8px;"><CircleCheck /></el-icon>
            今日签到
          </h3>
          <div v-if="activeTasks.length === 0" style="text-align: center; color: var(--text-muted);">
            暂无需要签到的任务
          </div>
          <div v-else>
            <div v-for="task in activeTasks" :key="task.id" style="display: flex; justify-content: space-between; align-items: center; padding: 12px 0; border-bottom: 1px solid var(--border-color);">
              <span style="font-weight: 500;">{{ task.name }}</span>
              <el-button type="primary" size="small" @click="goToCheckIn">
                签到
              </el-button>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'

const router = useRouter()

const stats = ref({
  files: 0,
  contacts: 0,
  tasks: 0,
  activeTasks: 0
})

const activeTasks = ref([])
const recentNotes = ref([])

const loadData = async () => {
  try {
    const filesRes = await api.files.getAll()
    stats.value.files = filesRes.data?.length || 0

    const contactsRes = await api.contacts.getAll()
    stats.value.contacts = contactsRes.data?.length || 0

    const tasksRes = await api.tasks.getAll()
    stats.value.tasks = tasksRes.data?.length || 0

    const activeRes = await api.tasks.getActive()
    activeTasks.value = activeRes.data || []
    stats.value.activeTasks = activeTasks.value.length

    const notesRes = await api.notes.getAll()
    recentNotes.value = (notesRes.data || []).slice(0, 3)
  } catch (e) {
    console.error('加载数据失败', e)
  }
}

const remainingDays = (task) => {
  if (!task.nextCheckTime) return 0
  const now = new Date()
  const next = new Date(task.nextCheckTime)
  const diff = next - now
  return Math.max(0, Math.ceil(diff / (1000 * 60 * 60 * 24)))
}

const goToCheckIn = () => {
  router.push('/checkin')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.heart-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.task-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background-color: var(--background-color);
  border-radius: 12px;
  margin-bottom: 12px;
}

.task-name {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.task-detail {
  font-size: 13px;
  color: var(--text-secondary);
}

.task-countdown {
  text-align: center;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
  color: white;
  padding: 12px 20px;
  border-radius: 12px;
}

.countdown-text {
  font-size: 12px;
  opacity: 0.9;
}

.countdown-days {
  font-size: 28px;
  font-weight: 700;
  margin: 4px 0;
}

.countdown-unit {
  font-size: 12px;
  opacity: 0.9;
}

.note-item {
  padding: 12px;
  background-color: var(--background-color);
  border-radius: 8px;
  margin-bottom: 8px;
}

.note-title {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.note-preview {
  font-size: 13px;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
