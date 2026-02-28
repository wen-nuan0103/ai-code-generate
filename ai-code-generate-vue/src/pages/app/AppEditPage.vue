<template>
  <div id="appEditPage">
    <div class="background-mesh"></div>

    <div class="content-wrapper">
      <div class="page-header animate-fade-in-up">
        <h1 class="title">编辑应用</h1>
        <!-- <p class="subtitle">配置您的 AI 应用参数与基本信息</p> -->
      </div>

      <div class="main-layout">
        <div class="form-section animate-fade-in-up" style="animation-delay: 0.1s">
          <div class="glass-panel">
            <div class="panel-header">
              <span class="panel-title">基本设置</span>
            </div>
            
            <a-form :model="formData" :rules="rules" layout="vertical" @finish="handleSubmit" ref="formRef" class="custom-form">
              <a-form-item label="应用名称" name="appName">
                <a-input v-model:value="formData.appName" placeholder="给你的应用起个名字" :maxlength="50" show-count class="glass-input" />
              </a-form-item>

              <a-form-item v-if="isAdmin" label="应用封面" name="cover" extra="支持 JPG, PNG, WEBP 格式，大小不超过 2MB">
                <div class="upload-container">
                  <a-upload
                    v-model:file-list="fileList"
                    name="file"
                    list-type="picture-card"
                    class="avatar-uploader"
                    :show-upload-list="false"
                    :before-upload="beforeUpload"
                    :custom-request="customUpload"
                  >
                    <img v-if="formData.cover" :src="formData.cover" alt="avatar" class="uploaded-image" />
                    <div v-else class="upload-placeholder">
                      <loading-outlined v-if="uploadLoading"></loading-outlined>
                      <plus-outlined v-else></plus-outlined>
                      <div class="ant-upload-text">上传封面</div>
                    </div>
                  </a-upload>
                  <a-button v-if="formData.cover" type="link" size="small" danger @click.stop="formData.cover = ''">
                    移除封面
                  </a-button>
                </div>
              </a-form-item>
              <!-- <a-form-item v-if="isAdmin" label="优先级" name="priority" extra="设置为99表示精选应用">
                <a-input-number v-model:value="formData.priority" :min="0" :max="99" class="glass-input" style="width: 100%" />
              </a-form-item> -->

              <a-form-item label="初始提示词" name="initPrompt">
                <div class="prompt-box">
                  <a-textarea v-model:value="formData.initPrompt" :rows="4" :maxlength="1000" show-count disabled class="glass-textarea" />
                  <div class="lock-tag"><span class="icon">🔒</span> 初始提示词不可修改</div>
                </div>
              </a-form-item>

              <div class="form-row">
                <a-form-item label="生成类型" name="codeGenType" class="half-width">
                  <div class="read-only-field">
                    {{ formatCodeGenType(formData.codeGenType) || '未知类型' }}
                  </div>
                </a-form-item>

                <a-form-item v-if="formData.deployKey" label="部署密钥" name="deployKey" class="half-width">
                  <div class="read-only-field key-font">
                    {{ formData.deployKey }}
                  </div>
                </a-form-item>
              </div>

              <div class="form-actions">
                <a-button class="action-btn submit-btn" html-type="submit" :loading="submitting">
                  保存修改
                </a-button>
                <a-button class="action-btn cancel-btn" @click="resetForm">重置</a-button>
                <a-button type="link" class="link-btn" @click="goToChat">前往对话 ></a-button>
              </div>
            </a-form>
          </div>
        </div>

        <div class="info-section animate-fade-in-up" style="animation-delay: 0.2s">
          <div class="glass-panel info-panel">
            <div class="panel-header">
              <span class="panel-title">应用元数据</span>
            </div>
            
            <div class="info-grid">
              <div class="info-item">
                <label>应用 ID</label>
                <div class="value code-font">{{ appInfo?.id }}</div>
              </div>
              <div class="info-item">
                <label>创建者</label>
                <div class="value">
                  <UserInfo :user="appInfo?.user" size="small" />
                </div>
              </div>
              <div class="info-item">
                <label>状态</label>
                <div class="value">
                  <span v-if="appInfo?.deployKey" class="status-badge success">已部署</span>
                  <span v-else class="status-badge warning">未部署</span>
                </div>
              </div>
              <div class="info-item full-width">
                <label>时间线</label>
                <div class="timeline-text">
                  <p>创建于：{{ formatTime(appInfo?.createTime) }}</p>
                  <p>更新于：{{ formatTime(appInfo?.updateTime) }}</p>
                </div>
              </div>
              
              <div class="info-item full-width" v-if="appInfo?.deployKey">
                <a-button class="preview-btn" block @click="openPreview">
                  <template #icon>
                    <span class="material-icons-round" style="font-size: 16px; margin-right: 4px;">launch</span>
                  </template>
                  查看实时预览
                </a-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { PlusOutlined, LoadingOutlined } from '@ant-design/icons-vue' // 引入图标
import type { UploadChangeParam, UploadProps } from 'ant-design-vue' // 引入类型
import { useUserStore } from '@/stores/user'
import { getAppVoById, updateApp, updateAppByAdmin } from '@/api/appController'
import { formatCodeGenType } from '@/utils/codeGenerateTypes'
import { formatTime } from '@/utils/time'
import UserInfo from '@/components/UserInfo.vue'
import { getStaticPreviewUrl } from '@/config/env'
import type { FormInstance } from 'ant-design-vue'
import myAxios from '@/request'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 应用信息
const appInfo = ref<API.AppVO>()
const loading = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

// 上传相关状态
const fileList = ref([])
const uploadLoading = ref(false)

// 表单数据
const formData = reactive({
  appName: '',
  cover: '',
  initPrompt: '',
  codeGenType: '',
  deployKey: '',
})

// 是否为管理员
const isAdmin = computed(() => {
  return userStore.user.role === 'admin'
})

// 表单验证规则
const rules = {
  appName: [
    { required: true, message: '请输入应用名称', trigger: 'blur' },
    { min: 1, max: 50, message: '应用名称长度在1-50个字符', trigger: 'blur' },
  ],
  // cover: [{ required: false, message: '请上传封面', trigger: 'change' }], // 根据需求决定是否必填
  priority: [{ type: 'number', min: 0, max: 99, message: '优先级范围0-99', trigger: 'blur' }],
}

/**
 * 上传前校验：检查文件类型和大小 (对应后端校验)
 */
const beforeUpload = (file: File) => {
  const isJpgOrPngOrWebp = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/webp';
  if (!isJpgOrPngOrWebp) {
    message.error('只支持上传 JPG/PNG/WEBP 格式的图片!');
  }
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isLt2M) {
    message.error('图片大小不能超过 2MB!');
  }
  return isJpgOrPngOrWebp && isLt2M;
};

/**
 * 自定义上传逻辑，调用后端 /file/upload 接口
 */
const customUpload = async ({ file, onSuccess, onError }: any) => {
  uploadLoading.value = true;
  try {
    const params = new FormData();
    params.append('file', file);
    
    // 调用后端接口
    const res = await myAxios.post('/file/upload', params, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });

    if (res.data.code === 0) {
      // 假设后端返回的数据结构是 { code: 0, data: "https://..." }
      const imgUrl = res.data.data;
      formData.cover = imgUrl; // 更新表单数据
      message.success('上传成功');
      onSuccess(res.data, file);
    } else {
      message.error('上传失败: ' + res.data.message);
      onError(new Error(res.data.message));
    }
  } catch (error: any) {
    message.error('上传出错: ' + error.message);
    onError(error);
  } finally {
    uploadLoading.value = false;
  }
};

// 获取应用信息
const fetchAppInfo = async () => {
  const id = route.params.id as string
  if (!id) {
    message.error('应用ID不存在')
    router.push('/')
    return
  }

  loading.value = true
  try {
    const res = await getAppVoById({ id: id as unknown as number })
    if (res.data.code === 0 && res.data.data) {
      appInfo.value = res.data.data

      // 检查权限
      if (!isAdmin.value && appInfo.value.userId !== userStore.user.id) {
        message.error('您没有权限编辑此应用')
        router.push('/')
        return
      }

      // 填充表单数据
      formData.appName = appInfo.value.appName || ''
      formData.cover = appInfo.value.cover || ''
      formData.initPrompt = appInfo.value.initPrompt || ''
      formData.codeGenType = appInfo.value.codeGeneratorType || ''
      formData.deployKey = appInfo.value.deployKey || ''
    } else {
      message.error('获取应用信息失败')
      router.push('/')
    }
  } catch (error) {
    console.error('获取应用信息失败：', error)
    message.error('获取应用信息失败')
    router.push('/')
  } finally {
    loading.value = false
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!appInfo.value?.id) return

  submitting.value = true
  try {
    let res
    if (isAdmin.value) {
      // 管理员可以修改更多字段
      res = await updateAppByAdmin({
        id: appInfo.value.id,
        appName: formData.appName,
        cover: formData.cover,
      })
    } else {
      // 普通用户只能修改应用名称
      res = await updateApp({
        id: appInfo.value.id,
        appName: formData.appName,
      })
    }

    if (res.data.code === 0) {
      message.success('修改成功')
      // 重新获取应用信息
      await fetchAppInfo()
    } else {
      message.error('修改失败：' + res.data.message)
    }
  } catch (error) {
    console.error('修改失败：', error)
    message.error('修改失败')
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  if (appInfo.value) {
    formData.appName = appInfo.value.appName || ''
    formData.cover = appInfo.value.cover || ''
  }
  formRef.value?.clearValidate()
}

// 进入对话页面
const goToChat = () => {
  if (appInfo.value?.id) {
    router.push(`/app/chat/${appInfo.value.id}`)
  }
}

// 打开预览
const openPreview = () => {
  if (appInfo.value?.codeGeneratorType && appInfo.value?.id) {
    const url = getStaticPreviewUrl(appInfo.value.codeGeneratorType, String(appInfo.value.id))
    window.open(url, '_blank')
  }
}

// 页面加载时获取应用信息
onMounted(() => {
  fetchAppInfo()
})
</script>

<style scoped>
/* 1. 布局与背景 */
#appEditPage {
  /* min-height: 100vh; */
  position: relative;
  /* padding: 40px 20px; */
  padding-top: 40px;
  overflow: hidden;
}

.background-mesh {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
  background-color: #fce7f3;
  background-image: 
      radial-gradient(at 10% 10%, hsla(250, 80%, 90%, 1) 0px, transparent 50%),
      radial-gradient(at 90% 10%, hsla(30, 90%, 90%, 1) 0px, transparent 50%),
      radial-gradient(at 50% 50%, hsla(340, 80%, 95%, 1) 0px, transparent 50%),
      radial-gradient(at 80% 80%, hsla(210, 80%, 90%, 1) 0px, transparent 50%);
}

.content-wrapper {
  max-width: 1000px;
  margin: 0 auto;
}

/* 2. 头部区域 */
.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.title {
  font-size: 32px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 8px;
  letter-spacing: -0.5px;
}

.subtitle {
  color: #64748b;
  font-size: 16px;
}

/* 3. 玻璃面板样式 */
.glass-panel {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 24px;
  padding: 32px;
  box-shadow: 0 4px 24px -1px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s ease;
}

.panel-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.panel-title {
  font-size: 18px;
  font-weight: 600;
  color: #334155;
}

/* 4. 表单样式优化 */
.custom-form :deep(.ant-form-item-label > label) {
  color: #475569;
  font-weight: 500;
}

/* 覆盖 Ant Design 输入框样式 */
.glass-input, 
.glass-textarea {
  background: rgba(255, 255, 255, 0.5) !important;
  border: 1px solid rgba(203, 213, 225, 0.6) !important;
  border-radius: 12px !important;
  padding: 8px 12px !important;
  transition: all 0.3s !important;
}

.glass-input:focus, 
.glass-textarea:focus {
  background: #fff !important;
  border-color: #3b82f6 !important;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1) !important;
}

.glass-textarea {
  resize: none;
}

/* 上传组件样式 */
.upload-container {
  display: flex;
  align-items: flex-end;
  gap: 16px;
}
.avatar-uploader > .ant-upload {
  width: 128px;
  height: 128px;
}
.upload-placeholder {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #64748b;
}
.uploaded-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}
:deep(.ant-upload.ant-upload-select-picture-card) {
  background-color: rgba(255, 255, 255, 0.5) !important;
  border: 1px dashed rgba(203, 213, 225, 0.8) !important;
  border-radius: 12px !important;
}
:deep(.ant-upload.ant-upload-select-picture-card:hover) {
  border-color: #3b82f6 !important;
  background-color: #fff !important;
}

/* 锁定区域 */
.prompt-box {
  position: relative;
}
.lock-tag {
  margin-top: 8px;
  font-size: 12px;
  color: #94a3b8;
  display: flex;
  align-items: center;
  gap: 4px;
}

.form-row {
  display: flex;
  gap: 20px;
}
.half-width {
  flex: 1;
}

.read-only-field {
  background: rgba(241, 245, 249, 0.6);
  padding: 8px 12px;
  border-radius: 12px;
  color: #64748b;
  font-size: 14px;
  min-height: 40px;
  display: flex;
  align-items: center;
}

.key-font, .code-font {
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  font-size: 13px;
}

/* 5. 按钮组 */
.form-actions {
  margin-top: 32px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.action-btn {
  height: 40px;
  border-radius: 20px;
  padding: 0 24px;
  font-weight: 500;
  border: none;
}

.submit-btn {
  background: #0f172a;
  color: #fff;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.2);
}
.submit-btn:hover {
  background: #1e293b !important;
  color: #fff !important;
  transform: translateY(-1px);
}

.cancel-btn {
  background: #fff;
  color: #475569;
  border: 1px solid #e2e8f0;
}
.cancel-btn:hover {
  border-color: #cbd5e1;
  color: #1e293b;
}

.link-btn {
  margin-left: auto;
  color: #3b82f6;
}

/* 6. 右侧/底部信息面板 */
.main-layout {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
  align-items: start;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
}

.info-item label {
  display: block;
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 4px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-item .value {
  font-size: 14px;
  color: #334155;
  font-weight: 500;
}

.status-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
}
.status-badge.success {
  background: #dcfce7;
  color: #166534;
}
.status-badge.warning {
  background: #fef9c3;
  color: #854d0e;
}

.timeline-text p {
  margin: 4px 0;
  font-size: 13px;
  color: #64748b;
}

.preview-btn {
  border-radius: 12px;
  height: 36px;
  font-size: 13px;
  color: #3b82f6;
  border-color: #bfdbfe;
  background: #eff6ff;
  display: flex;
  align-items: center;
  justify-content: center;
}
.preview-btn:hover {
  background: #dbeafe;
  border-color: #3b82f6;
}

/* 动画类 */
.animate-fade-in-up {
  animation: fadeInUp 0.6s ease-out forwards;
  opacity: 0;
  transform: translateY(20px);
}

@keyframes fadeInUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式 */
@media (max-width: 768px) {
  .main-layout {
    grid-template-columns: 1fr;
  }
  
  .form-row {
    flex-direction: column;
    gap: 0;
  }
  
  .form-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .link-btn {
    margin-left: 0;
    text-align: center;
  }
}
</style>