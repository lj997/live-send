import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

api.interceptors.response.use(
  response => {
    const data = response.data
    if (data.success) {
      return data
    } else {
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message || '请求失败'))
    }
  },
  error => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default {
  user: {
    getCurrent() {
      return api.get('/users/current')
    },
    updateEmailConfig(id, config) {
      return api.put(`/users/${id}/email-config`, config)
    },
    testEmail(id, toEmail) {
      return api.post(`/users/${id}/test-email`, null, {
        params: { toEmail }
      })
    }
  },

  files: {
    getAll() {
      return api.get('/files')
    },
    getById(id) {
      return api.get(`/files/${id}`)
    },
    upload(file) {
      const formData = new FormData()
      formData.append('file', file)
      return api.post('/files/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })
    },
    delete(id) {
      return api.delete(`/files/${id}`)
    },
    getPreviewUrl(id) {
      return `/api/files/${id}/preview`
    },
    getDownloadUrl(id) {
      return `/api/files/${id}/download`
    }
  },

  notes: {
    getAll() {
      return api.get('/notes')
    },
    getById(id) {
      return api.get(`/notes/${id}`)
    },
    create(note) {
      return api.post('/notes', note)
    },
    update(id, note) {
      return api.put(`/notes/${id}`, note)
    },
    delete(id) {
      return api.delete(`/notes/${id}`)
    }
  },

  contacts: {
    getAll() {
      return api.get('/contacts')
    },
    getById(id) {
      return api.get(`/contacts/${id}`)
    },
    create(contact) {
      return api.post('/contacts', contact)
    },
    update(id, contact) {
      return api.put(`/contacts/${id}`, contact)
    },
    delete(id) {
      return api.delete(`/contacts/${id}`)
    }
  },

  tasks: {
    getAll() {
      return api.get('/tasks')
    },
    getActive() {
      return api.get('/tasks/active')
    },
    getById(id) {
      return api.get(`/tasks/${id}`)
    },
    create(task) {
      return api.post('/tasks', task)
    },
    update(id, task) {
      return api.put(`/tasks/${id}`, task)
    },
    pause(id) {
      return api.post(`/tasks/${id}/pause`)
    },
    resume(id) {
      return api.post(`/tasks/${id}/resume`)
    },
    cancel(id) {
      return api.post(`/tasks/${id}/cancel`)
    }
  },

  checkin: {
    getRecordsByTask(taskId) {
      return api.get(`/checkin/task/${taskId}`)
    },
    hasCheckedInToday(taskId) {
      return api.get(`/checkin/task/${taskId}/today`)
    },
    checkIn(taskId, note) {
      return api.post(`/checkin/task/${taskId}`, { note })
    }
  }
}
