<template>
  <div>
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><User /></el-icon>
        联系人
      </h2>
      <el-button type="primary" class="btn-primary" @click="createContact">
        <el-icon><Plus /></el-icon>
        添加联系人
      </el-button>
    </div>

    <el-dialog v-model="showDialog" :title="isEdit ? '编辑联系人' : '添加联系人'" width="500px">
      <el-form :model="contactForm" label-width="80px">
        <el-form-item label="姓名">
          <el-input v-model="contactForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="contactForm.email" placeholder="请输入邮箱地址" />
        </el-form-item>
        <el-form-item label="关系">
          <el-input v-model="contactForm.relationship" placeholder="例如：家人、朋友" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="contactForm.note"
            type="textarea"
            :rows="3"
            placeholder="备注信息..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="saveContact">保存</el-button>
      </template>
    </el-dialog>

    <div v-if="contacts.length === 0" class="empty-state">
      <div class="icon">👥</div>
      <div class="text">暂无联系人，点击上方按钮添加</div>
    </div>

    <el-row :gutter="20" v-else>
      <el-col :span="8" v-for="contact in contacts" :key="contact.id">
        <div class="card contact-card">
          <div class="contact-avatar-large">
            {{ contact.name.charAt(0) }}
          </div>
          <div class="contact-info-large">
            <div class="contact-name-large">{{ contact.name }}</div>
            <div class="contact-email-large">
              <el-icon><Message /></el-icon>
              {{ contact.email }}
            </div>
            <div v-if="contact.relationship" class="contact-relationship">
              <el-icon><Connection /></el-icon>
              {{ contact.relationship }}
            </div>
          </div>
          <div class="contact-actions">
            <el-button size="small" text @click="editContact(contact)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button size="small" text type="danger" @click="deleteContact(contact)">
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const contacts = ref([])
const showDialog = ref(false)
const isEdit = ref(false)
const editingId = ref(null)

const contactForm = ref({
  name: '',
  email: '',
  relationship: '',
  note: ''
})

const loadContacts = async () => {
  try {
    const res = await api.contacts.getAll()
    contacts.value = res.data || []
  } catch (e) {
    console.error('加载联系人失败', e)
  }
}

const createContact = () => {
  isEdit.value = false
  editingId.value = null
  contactForm.value = { name: '', email: '', relationship: '', note: '' }
  showDialog.value = true
}

const editContact = (contact) => {
  isEdit.value = true
  editingId.value = contact.id
  contactForm.value = {
    name: contact.name,
    email: contact.email,
    relationship: contact.relationship || '',
    note: contact.note || ''
  }
  showDialog.value = true
}

const saveContact = async () => {
  if (!contactForm.value.name.trim()) {
    ElMessage.warning('请输入姓名')
    return
  }
  if (!contactForm.value.email.trim()) {
    ElMessage.warning('请输入邮箱')
    return
  }

  try {
    if (isEdit.value) {
      await api.contacts.update(editingId.value, contactForm.value)
      ElMessage.success('更新成功')
    } else {
      await api.contacts.create(contactForm.value)
      ElMessage.success('添加成功')
    }
    showDialog.value = false
    loadContacts()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

const deleteContact = async (contact) => {
  try {
    await ElMessageBox.confirm('确定要删除该联系人吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await api.contacts.delete(contact.id)
    ElMessage.success('删除成功')
    loadContacts()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadContacts()
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.contact-card {
  text-align: center;
}

.contact-avatar-large {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, var(--primary-light) 0%, var(--primary-color) 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  font-weight: 600;
  margin: 0 auto 16px;
}

.contact-info-large {
  margin-bottom: 12px;
}

.contact-name-large {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.contact-email-large {
  font-size: 13px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  margin-bottom: 4px;
}

.contact-relationship {
  font-size: 13px;
  color: var(--text-muted);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.contact-actions {
  display: flex;
  justify-content: center;
  gap: 8px;
}
</style>
