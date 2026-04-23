<template>
  <div>
    <h2 class="page-title">
      <el-icon><Setting /></el-icon>
      设置
    </h2>

    <div class="card settings-card">
      <h3 style="color: var(--primary-color); font-size: 18px; margin: 0 0 24px 0;">
        <el-icon style="margin-right: 8px;"><Message /></el-icon>
        邮箱配置
      </h3>
      
      <p style="color: var(--text-muted); font-size: 13px; margin-bottom: 24px; padding: 12px; background-color: var(--background-color); border-radius: 8px;">
        配置你的邮箱信息，用于发送邮件给联系人。建议使用专用应用密码。
      </p>

      <el-form :model="emailForm" label-width="120px" style="max-width: 500px;">
        <el-form-item label="发件人邮箱">
          <el-input v-model="emailForm.email" placeholder="例如: yourname@example.com" />
        </el-form-item>
        
        <el-form-item label="SMTP服务器">
          <el-input v-model="emailForm.emailHost" placeholder="例如: smtp.gmail.com" />
          <div style="font-size: 12px; color: var(--text-muted); margin-top: 8px;">
            常用SMTP服务器: Gmail (smtp.gmail.com), QQ邮箱 (smtp.qq.com), 163邮箱 (smtp.163.com)
          </div>
        </el-form-item>

        <el-form-item label="端口">
          <el-select v-model="emailForm.emailPort" placeholder="选择端口" style="width: 200px;">
            <el-option :label="587 (推荐)" :value="587" />
            <el-option :label="465" :value="465" />
            <el-option :label="25" :value="25" />
          </el-select>
        </el-form-item>

        <el-form-item label="邮箱用户名">
          <el-input v-model="emailForm.emailUsername" placeholder="通常是邮箱地址" />
        </el-form-item>

        <el-form-item label="邮箱密码">
          <el-input
            v-model="emailForm.emailPassword"
            type="password"
            placeholder="请输入密码或应用专用密码"
            show-password
          />
          <div style="font-size: 12px; color: var(--text-muted); margin-top: 8px;">
            ⚠️ 部分邮箱需要使用应用专用密码而非登录密码（如 Gmail、QQ邮箱）
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" class="btn-primary" @click="saveEmailConfig" :loading="saving">
            保存配置
          </el-button>
          <el-button @click="testEmail" :loading="testing">
            发送测试邮件
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="card settings-card" style="margin-top: 24px;">
      <h3 style="color: var(--primary-color); font-size: 18px; margin: 0 0 16px 0;">
        <el-icon style="margin-right: 8px;"><InfoFilled /></el-icon>
        关于 Live Send
      </h3>
      <div style="color: var(--text-secondary); line-height: 1.8;">
        <p><strong>版本:</strong> 1.0.0</p>
        <p style="margin-top: 12px;">
          Live Send 是一个温馨的应用，帮助你在意外发生时，将重要的文件和信息传递给你爱的人。
        </p>
        <p style="margin-top: 12px;">
          通过定期签到机制，系统会在你未能按时签到时，自动将你预先准备好的文件发送给指定的联系人。
        </p>
        <div style="margin-top: 20px; padding: 16px; background: linear-gradient(135deg, rgba(255, 107, 157, 0.1) 0%, rgba(255, 182, 193, 0.1) 100%); border-radius: 8px; text-align: center;">
          <span style="font-size: 24px;">💝</span>
          <p style="margin-top: 8px; color: var(--primary-color);">愿爱与温暖永远伴你左右</p>
        </div>
      </div>
    </div>

    <el-dialog v-model="showTestEmailDialog" title="发送测试邮件" width="400px">
      <el-form label-width="80px">
        <el-form-item label="测试邮箱">
          <el-input v-model="testEmail" placeholder="请输入接收测试邮件的邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTestEmailDialog = false">取消</el-button>
        <el-button type="primary" @click="sendTestEmail" :loading="testing">
          发送
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'

const emailForm = ref({
  email: '',
  emailHost: '',
  emailPort: 587,
  emailUsername: '',
  emailPassword: ''
})

const saving = ref(false)
const testing = ref(false)
const showTestEmailDialog = ref(false)
const testEmail = ref('')
const user = ref(null)

const loadUser = async () => {
  try {
    const res = await api.user.getCurrent()
    user.value = res.data
    if (user.value) {
      emailForm.value.email = user.value.email || ''
      emailForm.value.emailHost = user.value.emailHost || ''
      emailForm.value.emailPort = user.value.emailPort || 587
      emailForm.value.emailUsername = user.value.emailUsername || ''
      emailForm.value.emailPassword = user.value.emailPassword || ''
    }
  } catch (e) {
    console.error('加载用户信息失败', e)
  }
}

const saveEmailConfig = async () => {
  if (!emailForm.value.email) {
    ElMessage.warning('请输入发件人邮箱')
    return
  }
  if (!emailForm.value.emailHost) {
    ElMessage.warning('请输入SMTP服务器地址')
    return
  }

  saving.value = true
  try {
    await api.user.updateEmailConfig(user.value.id, emailForm.value)
    ElMessage.success('配置保存成功')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const testEmail = () => {
  if (!emailForm.value.email || !emailForm.value.emailHost) {
    ElMessage.warning('请先保存邮箱配置')
    return
  }
  showTestEmailDialog.value = true
}

const sendTestEmail = async () => {
  if (!testEmail.value) {
    ElMessage.warning('请输入测试邮箱地址')
    return
  }

  testing.value = true
  try {
    await api.user.testEmail(user.value.id, testEmail.value)
    ElMessage.success('测试邮件已发送，请查收')
    showTestEmailDialog.value = false
  } catch (e) {
    ElMessage.error('发送失败，请检查配置是否正确')
  } finally {
    testing.value = false
  }
}

onMounted(() => {
  loadUser()
})
</script>

<style scoped>
.settings-card {
  max-width: 700px;
}
</style>
