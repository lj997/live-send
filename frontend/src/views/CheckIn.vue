<template>
  <div>
    <div class="welcome-header" style="margin-bottom: 32px;">
      <div class="heart-icon">🌅</div>
      <h1>每日签到</h1>
      <p>签到证明你平安，守护你爱的人</p>
    </div>

    <div v-if="activeTasks.length === 0" class="empty-state">
      <div class="icon">⏰</div>
      <div class="text">暂无需要签到的任务</div>
      <div style="margin-top: 16px;">
        <el-button type="primary" @click="$router.push('/tasks')">
          去创建任务
        </el-button>
      </div>
    </div>

    <el-row :gutter="24" v-else>
      <el-col :span="12" v-for="task in activeTasks" :key="task.id">
        <div class="card checkin-card">
          <div class="checkin-card-header">
            <div class="task-name">{{ task.name }}</div>
            <span :class="['status-tag', 'active']">进行中</span>
          </div>

          <div class="checkin-progress">
            <div class="progress-info">
              <span>签到进度</span>
              <span style="font-weight: 600; color: var(--primary-color);">
                {{ task.currentCheckIns }} / {{ task.requiredCheckIns }}
              </span>
            </div>
            <el-progress
              :percentage="(task.currentCheckIns / task.requiredCheckIns) * 100"
              :stroke-width="12"
              :show-text="false"
              color="#FF6B9D"
            />
            <div style="font-size: 12px; color: var(--text-muted); margin-top: 8px;">
              距离下次检查: {{ remainingDays(task) }} 天
            </div>
          </div>

          <div class="checkin-actions">
            <el-button
              type="primary"
              class="checkin-btn"
              @click="handleCheckIn(task)"
              :disabled="task.checkedToday"
              :loading="task.loading"
              size="large"
            >
              <el-icon><CircleCheck /></el-icon>
              {{ task.checkedToday ? '今日已签到' : '立即签到' }}
            </el-button>
          </div>

          <div class="checkin-detail">
            <div class="detail-row">
              <el-icon><Folder /></el-icon>
              <span>包含 {{ task.files.length }} 个文件</span>
            </div>
            <div class="detail-row">
              <el-icon><User /></el-icon>
              <span>发送给 {{ task.contacts.length }} 位联系人</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-dialog v-model="showCheckInDialog" title="签到确认" width="400px">
      <div style="text-align: center; padding: 20px 0;">
        <div style="font-size: 48px; margin-bottom: 16px;">🌅</div>
        <p style="font-size: 16px; color: var(--text-primary); margin-bottom: 8px;">
          确认完成今日签到？
        </p>
        <p style="font-size: 13px; color: var(--text-muted);">
          签到后，你的任务进度将更新
        </p>
      </div>
      <template #footer>
        <el-button @click="showCheckInDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmCheckIn">确认签到</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'

const activeTasks = ref([])
const showCheckInDialog = ref(false)
const currentTask = ref(null)

const loadTasks = async () => {
  try {
    const res = await api.tasks.getActive()
    const tasks = res.data || []
    
    for (const task of tasks) {
      try {
        const checkRes = await api.checkin.hasCheckedInToday(task.id)
        task.checkedToday = checkRes.data || false
      } catch (e) {
        task.checkedToday = false
      }
      task.loading = false
    }
    
    activeTasks.value = tasks
  } catch (e) {
    console.error('加载任务失败', e)
  }
}

const remainingDays = (task) => {
  if (!task.nextCheckTime) return 0
  const now = new Date()
  const next = new Date(task.nextCheckTime)
  const diff = next - now
  return Math.max(0, Math.ceil(diff / (1000 * 60 * 60 * 24)))
}

const handleCheckIn = (task) => {
  currentTask.value = task
  showCheckInDialog.value = true
}

const confirmCheckIn = async () => {
  if (!currentTask.value) return

  currentTask.value.loading = true
  try {
    await api.checkin.checkIn(currentTask.value.id, '每日签到')
    ElMessage.success({
      message: '签到成功！今天又是平安的一天 💝',
      type: 'success',
      duration: 3000
    })
    showCheckInDialog.value = false
    loadTasks()
  } catch (e) {
    ElMessage.error('签到失败')
  } finally {
    if (currentTask.value) {
      currentTask.value.loading = false
    }
  }
}

onMounted(() => {
  loadTasks()
})
</script>

<style scoped>
.checkin-card {
  text-align: center;
}

.checkin-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.task-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.checkin-progress {
  text-align: left;
  margin-bottom: 24px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 14px;
  color: var(--text-secondary);
}

.checkin-actions {
  margin-bottom: 24px;
}

.checkin-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 24px;
  background: linear-gradient(135deg, #67C23A 0%, #85CE61 100%);
  border: none;
}

.checkin-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #5DAF34 0%, #67C23A 100%);
}

.checkin-btn:disabled {
  background: #DCDFE6;
  cursor: not-allowed;
}

.checkin-detail {
  text-align: left;
  padding: 16px;
  background-color: var(--background-color);
  border-radius: 8px;
}

.detail-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.detail-row:last-child {
  margin-bottom: 0;
}
</style>
