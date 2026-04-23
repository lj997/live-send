<template>
  <div>
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Notebook /></el-icon>
        备忘录
      </h2>
      <el-button type="primary" class="btn-primary" @click="createNote">
        <el-icon><Plus /></el-icon>
        新建笔记
      </el-button>
    </div>

    <el-dialog v-model="showDialog" :title="isEdit ? '编辑笔记' : '新建笔记'" width="600px">
      <el-form :model="noteForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="noteForm.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input
            v-model="noteForm.content"
            type="textarea"
            :rows="12"
            placeholder="写下你想说的话..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="saveNote">保存</el-button>
      </template>
    </el-dialog>

    <div v-if="notes.length === 0" class="empty-state">
      <div class="icon">📝</div>
      <div class="text">暂无笔记，点击上方按钮创建</div>
    </div>

    <el-row :gutter="20" v-else>
      <el-col :span="8" v-for="note in notes" :key="note.id">
        <div class="card note-card" @click="editNote(note)">
          <div class="note-card-header">
            <div class="note-title">{{ note.title }}</div>
            <div class="note-actions" @click.stop>
              <el-button size="small" text type="danger" @click="deleteNote(note)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
          <div class="note-content">
            {{ note.content || '暂无内容' }}
          </div>
          <div class="note-time">
            {{ formatTime(note.updatedAt) }}
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const notes = ref([])
const showDialog = ref(false)
const isEdit = ref(false)
const editingId = ref(null)

const noteForm = ref({
  title: '',
  content: ''
})

const loadNotes = async () => {
  try {
    const res = await api.notes.getAll()
    notes.value = res.data || []
  } catch (e) {
    console.error('加载笔记失败', e)
  }
}

const createNote = () => {
  isEdit.value = false
  editingId.value = null
  noteForm.value = { title: '', content: '' }
  showDialog.value = true
}

const editNote = (note) => {
  isEdit.value = true
  editingId.value = note.id
  noteForm.value = {
    title: note.title,
    content: note.content
  }
  showDialog.value = true
}

const saveNote = async () => {
  if (!noteForm.value.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }

  try {
    if (isEdit.value) {
      await api.notes.update(editingId.value, noteForm.value)
      ElMessage.success('更新成功')
    } else {
      await api.notes.create(noteForm.value)
      ElMessage.success('创建成功')
    }
    showDialog.value = false
    loadNotes()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

const deleteNote = async (note) => {
  try {
    await ElMessageBox.confirm('确定要删除该笔记吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await api.notes.delete(note.id)
    ElMessage.success('删除成功')
    loadNotes()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
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
  loadNotes()
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.note-card {
  cursor: pointer;
  transition: all 0.3s ease;
}

.note-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.note-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.note-content {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
}

.note-time {
  font-size: 12px;
  color: var(--text-muted);
}
</style>
