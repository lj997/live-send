<template>
  <div>
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Folder /></el-icon>
        文件管理
      </h2>
      <el-button type="primary" class="btn-primary" @click="showUpload = true">
        <el-icon><Upload /></el-icon>
        上传文件
      </el-button>
    </div>

    <el-dialog v-model="showUpload" title="上传文件" width="500px">
      <el-upload
        class="upload-demo"
        drag
        :auto-upload="false"
        :on-change="handleFileChange"
        :file-list="fileList"
        multiple
      >
        <div class="upload-area">
          <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
          <div class="el-upload__text" style="margin-top: 16px;">
            将文件拖到此处，或<em>点击选择</em>
          </div>
          <div class="el-upload__tip" style="color: var(--text-muted); margin-top: 8px;">
            支持上传文档、图片、音视频等各种类型的文件
          </div>
        </div>
      </el-upload>
      <template #footer>
        <el-button @click="showUpload = false">取消</el-button>
        <el-button type="primary" @click="uploadFiles" :loading="uploading">
          开始上传
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showPreview" title="文件预览" width="700px">
      <div v-if="previewFile" class="preview-content">
        <div class="preview-header">
          <div class="file-icon">
            <el-icon :size="24"><Document /></el-icon>
          </div>
          <div class="file-info">
            <div class="file-name">{{ previewFile.originalName }}</div>
            <div class="file-meta">
              {{ formatFileSize(previewFile.fileSize) }} · {{ previewFile.fileType }}
            </div>
          </div>
        </div>
        <div class="preview-body">
          <div v-if="isImage(previewFile)" class="image-preview">
            <img :src="previewUrl" alt="预览" />
          </div>
          <div v-else class="no-preview">
            <el-icon :size="64" color="#999"><Document /></el-icon>
            <p>该类型文件暂不支持在线预览</p>
            <el-button type="primary" @click="downloadFile(previewFile)">
              下载查看
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>

    <div v-if="files.length === 0" class="empty-state">
      <div class="icon">📁</div>
      <div class="text">暂无文件，点击上方按钮上传</div>
    </div>

    <el-row :gutter="20" v-else>
      <el-col :span="6" v-for="file in files" :key="file.id">
        <div class="card file-card">
          <div class="file-icon-large" :class="{ 'is-image': isImage(file) }">
            <el-icon v-if="!isImage(file)" :size="36"><Document /></el-icon>
            <img v-else :src="getPreviewUrl(file)" alt="" />
          </div>
          <div class="file-info-card">
            <div class="file-name-card" :title="file.originalName">
              {{ file.originalName }}
            </div>
            <div class="file-size-card">
              {{ formatFileSize(file.fileSize) }}
            </div>
          </div>
          <div class="file-actions">
            <el-button size="small" text @click="previewFileItem(file)">
              <el-icon><View /></el-icon>
              预览
            </el-button>
            <el-button size="small" text @click="downloadFile(file)">
              <el-icon><Download /></el-icon>
              下载
            </el-button>
            <el-button size="small" text type="danger" @click="deleteFile(file)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const files = ref([])
const showUpload = ref(false)
const showPreview = ref(false)
const fileList = ref([])
const uploading = ref(false)
const previewFile = ref(null)

const loadFiles = async () => {
  try {
    const res = await api.files.getAll()
    files.value = res.data || []
  } catch (e) {
    console.error('加载文件失败', e)
  }
}

const handleFileChange = (file, filesList) => {
  fileList.value = filesList
}

const uploadFiles = async () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('请先选择文件')
    return
  }

  uploading.value = true
  try {
    for (const file of fileList.value) {
      await api.files.upload(file.raw)
    }
    ElMessage.success('上传成功')
    showUpload.value = false
    fileList.value = []
    loadFiles()
  } catch (e) {
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
  }
}

const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const isImage = (file) => {
  const type = file.fileType || ''
  return type.startsWith('image/')
}

const getPreviewUrl = (file) => {
  return api.files.getPreviewUrl(file.id)
}

const previewUrl = computed(() => {
  return previewFile.value ? api.files.getPreviewUrl(previewFile.value.id) : ''
})

const previewFileItem = (file) => {
  previewFile.value = file
  showPreview.value = true
}

const downloadFile = (file) => {
  window.open(api.files.getDownloadUrl(file.id), '_blank')
}

const deleteFile = async (file) => {
  try {
    await ElMessageBox.confirm('确定要删除该文件吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await api.files.delete(file.id)
    ElMessage.success('删除成功')
    loadFiles()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadFiles()
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.file-card {
  text-align: center;
  cursor: pointer;
}

.file-icon-large {
  width: 80px;
  height: 80px;
  margin: 0 auto 16px;
  background-color: var(--background-color);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary-color);
  overflow: hidden;
}

.file-icon-large.is-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.file-info-card {
  margin-bottom: 12px;
}

.file-name-card {
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-size-card {
  font-size: 12px;
  color: var(--text-muted);
}

.file-actions {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.preview-header {
  display: flex;
  align-items: center;
  padding: 16px;
  background-color: var(--background-color);
  border-radius: 8px;
  margin-bottom: 16px;
}

.preview-header .file-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, var(--primary-light) 0%, var(--primary-color) 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-right: 12px;
}

.preview-header .file-name {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.preview-header .file-meta {
  font-size: 13px;
  color: var(--text-muted);
}

.preview-body {
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-preview img {
  max-width: 100%;
  max-height: 400px;
  border-radius: 8px;
}

.no-preview {
  text-align: center;
  color: var(--text-muted);
}

.no-preview p {
  margin: 16px 0;
}
</style>
