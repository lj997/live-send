<template>
  <div>
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Clock /></el-icon>
        发送任务
      </h2>
      <el-button type="primary" class="btn-primary" @click="createTask">
        <el-icon><Plus /></el-icon>
        新建任务
      </el-button>
    </div>

    <el-dialog v-model="showDialog" :title="isEdit ? '编辑任务' : '新建任务'" width="700px">
      <el-form :model="taskForm" label-width="120px">
        <el-form-item label="任务名称">
          <el-input v-model="taskForm.name" placeholder="请输入任务名称" />
        </el-form-item>
        
        <el-form-item label="选择文件">
          <el-select
            v-model="taskForm.fileIds"
            multiple
            placeholder="请选择要发送的文件"
            style="width: 100%;"
          >
            <el-option
              v-for="file in allFiles"
              :key="file.id"
              :label="file.originalName"
              :value="file.id"
            />
          </el-select>
          <p v-if="allFiles.length === 0" style="color: var(--text-muted); font-size: 12px; margin-top: 8px;">
            暂无文件，请先在「文件管理」中上传
          </p>
        </el-form-item>

        <el-form-item label="选择联系人">
          <el-select
            v-model="taskForm.contactIds"
            multiple
            placeholder="请选择收件人"
            style="width: 100%;"
          >
            <el-option
              v-for="contact in allContacts"
              :key="contact.id"
              :label="`${contact.name} (${contact.email})`"
              :value="contact.id"
            />
          </el-select>
          <p v-if="allContacts.length === 0" style="color: var(--text-muted); font-size: 12px; margin-top: 8px;">
            暂无联系人，请先在「联系人」中添加
          </p>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="倒计时（天）">
              <el-input-number
                v-model="taskForm.countdownDays"
                :min="1"
                :max="3650"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="需要签到次数">
              <el-input-number
                v-model="taskForm.requiredCheckIns"
                :min="1"
                :max="taskForm.countdownDays"
                style="width: 100%;"
              />
              <p style="color: var(--text-muted); font-size: 12px;">
                必须小于等于倒计时天数
              </p>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="saveTask">保存</el-button>
      </template>
    </el-dialog>

    <div v-if="tasks.length === 0" class="empty-state">
      <div class="icon">⏰</div>
      <div class="text">暂无任务，点击上方按钮创建</div>
    </div>

    <div v-else>
      <div v-for="task in tasks" :key="task.id" class="card task-detail-card">
        <div class="task-header">
          <div class="task-name">{{ task.name }}</div>
          <div>
            <span :class="['status-tag', task.status.toLowerCase()]">
              {{ getStatusText(task.status) }}
            </span>
          </div>
        </div>

        <div class="task-content">
          <div class="task-section">
            <div class="section-title">
              <el-icon><Folder /></el-icon>
              发送文件 ({{ task.files.length }})
            </div>
            <div class="file-list">
              <div v-for="file in task.files" :key="file.id" class="file-item-small">
                <el-icon><Document /></el-icon>
                <span>{{ file.originalName }}</span>
              </div>
            </div>
          </div>

          <div class="task-section">
            <div class="section-title">
              <el-icon><User /></el-icon>
              收件人 ({{ task.contacts.length }})
            </div>
            <div class="contact-list">
              <div v-for="contact in task.contacts" :key="contact.id" class="contact-item-small">
                <el-icon><Message /></el-icon>
                <span>{{ contact.name }} ({{ contact.email }})</span>
              </div>
            </div>
          </div>

          <div class="task-info-row">
            <div class="info-item">
              <el-icon><Timer /></el-icon>
              <span>倒计时: <strong>{{ task.countdownDays }}</strong> 天</span>
            </div>
            <div class="info-item">
              <el-icon><CircleCheck /></el-icon>
              <span>签到进度: <strong>{{ task.currentCheckIns }}/{{ task.requiredCheckIns }}</strong></span>
            </div>
            <div class="info-item" v-if="task.nextCheckTime">
              <el-icon><Clock /></el-icon>
              <span>下次检查: <strong>{{ formatTime(task.nextCheckTime) }}</strong></span>
            </div>
          </div>
        </div>

        <div class="task-actions-bar">
          <template v-if="task.status === 'ACTIVE'">
            <el-button size="small" @click="goToCheckIn(task)">
              <el-icon><CircleCheck /></el-icon>
              签到
            </el-button>
            <el-button size="small" @click="editTask(task)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button size="small" @click="pauseTask(task)">
              <el-icon><VideoPause /></el-icon>
              暂停
            </el-button>
            <el-button size="small" type="danger" @click="cancelTask(task)">
              <el-icon><Close /></el-icon>
              取消
            </el-button>
          </template>
          <template v-else-if="task.status === 'PAUSED'">
            <el-button size="small" type="primary" @click="resumeTask(task)">
              <el-icon><VideoPlay /></el-icon>
              恢复
            </el-button>
            <el-button size="small" type="danger" @click="cancelTask(task)">
              <el-icon><Close /></el-icon>
              取消
            </el-button>
          </template>
          <template v-else>
            <span style="color: var(--text-muted);">
              {{ task.status === 'COMPLETED' ? '邮件已发送' : '已取消' }}
            </span>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const router = useRouter()

const tasks = ref([])
const allFiles = ref([])
const allContacts = ref([])
const showDialog = ref(false)
const isEdit = ref(false)
const editingId = ref(null)

const taskForm = ref({
  name: '',
  fileIds: [],
  contactIds: [],
  countdownDays: 7,
  requiredCheckIns: 3
})

const loadData = async () => {
  try {
    const [tasksRes, filesRes, contactsRes] = await Promise.all([
      api.tasks.getAll(),
      api.files.getAll(),
      api.contacts.getAll()
    ])
    tasks.value = tasksRes.data || []
    allFiles.value = filesRes.data || []
    allContacts.value = contactsRes.data || []
  } catch (e) {
    console.error('加载数据失败', e)
  }
}

const createTask = () => {
  isEdit.value = false
  editingId.value = null
  taskForm.value = {
    name: '',
    fileIds: [],
    contactIds: [],
    countdownDays: 7,
    requiredCheckIns: 3
  }
  showDialog.value = true
}

const editTask = (task) => {
  isEdit.value = true
  editingId.value = task.id
  taskForm.value = {
    name: task.name,
    fileIds: task.files.map(f => f.id),
    contactIds: task.contacts.map(c => c.id),
    countdownDays: task.countdownDays,
    requiredCheckIns: task.requiredCheckIns
  }
  showDialog.value = true
}

const saveTask = async () => {
  if (!taskForm.value.name.trim()) {
    ElMessage.warning('请输入任务名称')
    return
  }
  if (taskForm.value.fileIds.length === 0) {
    ElMessage.warning('请至少选择一个文件')
    return
  }
  if (taskForm.value.contactIds.length === 0) {
    ElMessage.warning('请至少选择一个联系人')
    return
  }
  if (taskForm.value.requiredCheckIns > taskForm.value.countdownDays) {
    ElMessage.warning('签到次数不能大于倒计时天数')
    return
  }

  try {
    if (isEdit.value) {
      await api.tasks.update(editingId.value, taskForm.value)
      ElMessage.success('更新成功')
    } else {
      await api.tasks.create(taskForm.value)
      ElMessage.success('创建成功')
    }
    showDialog.value = false
    loadData()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

const getStatusText = (status) => {
  const map = {
    'ACTIVE': '进行中',
    'PAUSED': '已暂停',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return map[status] || status
}

const goToCheckIn = (task) => {
  router.push('/checkin')
}

const pauseTask = async (task) => {
  try {
    await ElMessageBox.confirm('确定要暂停该任务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await api.tasks.pause(task.id)
    ElMessage.success('已暂停')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const resumeTask = async (task) => {
  try {
    await api.tasks.resume(task.id)
    ElMessage.success('已恢复')
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const cancelTask = async (task) => {
  try {
    await ElMessageBox.confirm('确定要取消该任务吗？取消后无法恢复。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await api.tasks.cancel(task.id)
    ElMessage.success('已取消')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.task-detail-card {
  margin-bottom: 16px;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-color);
}

.task-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.task-section {
  margin-bottom: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
  color: var(--text-secondary);
  margin-bottom: 8px;
  font-size: 14px;
}

.file-list, .contact-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.file-item-small, .contact-item-small {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background-color: var(--background-color);
  border-radius: 20px;
  font-size: 13px;
  color: var(--text-secondary);
}

.task-info-row {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  padding: 12px;
  background-color: var(--background-color);
  border-radius: 8px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--text-secondary);
}

.info-item strong {
  color: var(--primary-color);
}

.task-actions-bar {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--border-color);
  display: flex;
  gap: 8px;
}
</style>
