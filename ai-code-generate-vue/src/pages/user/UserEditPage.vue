<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { 
  LoadingOutlined, 
  PlusOutlined, 
  ArrowLeftOutlined, 
  SaveOutlined,
  CameraOutlined,
  UserOutlined,
  MobileOutlined,
  SafetyCertificateOutlined
} from '@ant-design/icons-vue'
import { updateUserInfo } from '@/api/userController'
import type { UploadChangeParam } from 'ant-design-vue'

const userStore = useUserStore()
const router = useRouter()

// 表单数据
const formState = ref<API.UserUpdateRequest>({
    userName: '',
    avatar: '',
    profile: ''
})

const loading = ref(false)
const uploadLoading = ref(false)

// 初始化数据
onMounted(() => {
    const user = userStore.user
    if (user) {
        formState.value = {
            id: user.id,
            userName: user.userName,
            avatar: user.avatar,
            profile: user.profile
        }
    }
})

// 处理头像上传
const handleUploadChange = (info: UploadChangeParam) => {
    if (info.file.status === 'uploading') {
        uploadLoading.value = true
        return
    }

    if (info.file.status === 'done') {
        const response = info.file.response

        if (response.code === 0) {
            const fileUrl = response.data
            formState.value.avatar = fileUrl

            uploadLoading.value = false
            message.success('头像上传成功')
        } else {
            uploadLoading.value = false
            message.error('上传失败：' + response.message)
        }
    }

    if (info.file.status === 'error') {
        uploadLoading.value = false
        message.error('网络错误，头像上传失败')
    }
}

// 提交修改
const handleSubmit = async () => {
    loading.value = true
    try {
        const res = await updateUserInfo(formState.value)
        if (res.data.code === 0) {
            message.success('修改成功')
            // 更新全局状态
            await userStore.fetchLoginUser()
            router.push('/user/profile')
        } else {
            message.error(res.data.message || '修改失败')
        }
    } catch (error) {
        message.error('系统错误')
    } finally {
        loading.value = false
    }
}
</script>

<template>
    <div class="page-wrapper">
        <div class="main-content">
            <div class="nav-header">
                <a class="back-link" @click="router.back()">
                    <ArrowLeftOutlined /> 返回个人中心
                </a>
            </div>

            <div class="content-card">
                <div class="card-header">
                    <h2 class="page-title">编辑个人资料</h2>
                    <p class="page-desc">更新您的个人信息和公开展示的资料</p>
                </div>

                <a-form layout="vertical" :model="formState" class="edit-form" @finish="handleSubmit">
                    
                    <div class="avatar-section">
                        <a-form-item class="avatar-form-item">
                            <a-upload 
                                name="file" 
                                list-type="picture-card" 
                                class="avatar-uploader" 
                                :show-upload-list="false"
                                action="/api/file/upload" 
                                :with-credentials="true" 
                                @change="handleUploadChange"
                            >
                                <div class="avatar-wrapper">
                                    <img v-if="formState.avatar" :src="formState.avatar" alt="avatar" class="avatar-img" />
                                    <div v-else class="avatar-placeholder">
                                        <loading-outlined v-if="uploadLoading" class="upload-icon" />
                                        <UserOutlined v-else class="placeholder-icon" />
                                    </div>
                                    
                                    <div class="avatar-overlay">
                                        <CameraOutlined class="camera-icon" />
                                        <span class="overlay-text">更换头像</span>
                                    </div>
                                </div>
                            </a-upload>
                            <div class="upload-tip">支持 jpg/png 格式，建议尺寸 200x200</div>
                        </a-form-item>
                    </div>

                    <div class="form-grid">
                        <div class="form-section">
                            <h3 class="section-title">基础信息</h3>
                            
                            <a-form-item label="昵称" name="userName" :rules="[{ required: true, message: '请输入昵称' }]">
                                <a-input v-model:value="formState.userName" placeholder="给自己起个好听的名字" size="large" />
                            </a-form-item>

                            <a-form-item label="个人简介" name="userProfile">
                                <a-textarea 
                                    v-model:value="formState.profile" 
                                    placeholder="写一段话介绍一下你自己..." 
                                    :rows="5" 
                                    :maxlength="200"
                                    show-count 
                                    size="large" 
                                />
                            </a-form-item>
                        </div>

                        <div class="form-section">
                            <h3 class="section-title">账号安全</h3>
                            
                            <a-form-item label="账号 ID">
                                <div class="input-with-icon disabled">
                                    <SafetyCertificateOutlined class="field-icon" />
                                    <span class="field-value">{{ userStore.user.userAccount }}</span>
                                    <span class="field-tag">不可修改</span>
                                </div>
                            </a-form-item>

                            <a-form-item label="绑定手机">
                                <div class="input-with-icon" :class="{ 'disabled': !userStore.user.phone }">
                                    <MobileOutlined class="field-icon" />
                                    <span class="field-value">{{ userStore.user.phone || '未绑定手机号' }}</span>
                                    <a-button type="link" size="small" class="action-link" @click="router.push('/user/bind-phone')">
                                        {{ userStore.user.phone ? '更换' : '去绑定' }}
                                    </a-button>
                                </div>
                                <div class="form-tip">用于账号登录和安全验证</div>
                            </a-form-item>
                        </div>
                    </div>

                    <div class="form-footer">
                        <a-button size="large" class="cancel-btn" @click="router.back()">取消</a-button>
                        <a-button type="primary" size="large" html-type="submit" :loading="loading" class="save-btn">
                            <SaveOutlined /> 保存修改
                        </a-button>
                    </div>
                </a-form>
            </div>
        </div>
    </div>
</template>

<style scoped>
.page-wrapper {
    /* min-height: 100vh; */
    background: linear-gradient(to bottom, #f0fdfa, #ffffff);
    color: #1f2937;
    font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    /* padding-bottom: 80px; */
}

.main-content {
    padding-top: 40px;
    padding-left: 16px;
    padding-right: 16px;
    max-width: 900px;
    margin: 0 auto;
}


.nav-header {
    margin-bottom: 24px;
}

.back-link {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    color: #6b7280;
    font-size: 14px;
    font-weight: 500;
    transition: color 0.2s;
    cursor: pointer;
}

.back-link:hover {
    color: #10b981; 
}

.content-card {
    background-color: #fff;
    border-radius: 2rem;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
    padding: 40px;
    border: 1px solid #f3f4f6;
}

.card-header {
    text-align: center;
    margin-bottom: 40px;
    border-bottom: 1px solid #f3f4f6;
    padding-bottom: 24px;
}

.page-title {
    font-size: 24px;
    font-weight: 700;
    color: #111827;
    margin-bottom: 8px;
}

.page-desc {
    color: #6b7280;
    font-size: 14px;
}

/* 头像上传部分 */
.avatar-section {
    display: flex;
    justify-content: center;
    margin-bottom: 40px;
}

.avatar-form-item {
    margin-bottom: 0;
    text-align: center;
}

/* 覆盖 Ant Design Upload 默认样式 */
.avatar-uploader :deep(.ant-upload) {
    width: 120px !important;
    height: 120px !important;
    border-radius: 50% !important;
    overflow: hidden;
    padding: 0 !important;
    border: 4px solid #fff !important;
    box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1) !important;
    background-color: #f3f4f6 !important;
    position: relative;
    transition: all 0.3s;
}

.avatar-uploader :deep(.ant-upload):hover {
    transform: translateY(-2px);
    box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1) !important;
}

.avatar-wrapper {
    width: 100%;
    height: 100%;
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
}

.avatar-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.avatar-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #e5e7eb;
    color: #9ca3af;
}

.placeholder-icon {
    font-size: 48px;
}

.upload-icon {
    font-size: 24px;
    color: #10b981;
}

/* 头像悬停遮罩 */
.avatar-overlay {
    position: absolute;
    inset: 0;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.2s;
    color: #fff;
}

.avatar-wrapper:hover .avatar-overlay {
    opacity: 1;
}

.camera-icon {
    font-size: 24px;
    margin-bottom: 4px;
}

.overlay-text {
    font-size: 12px;
    font-weight: 500;
}

.upload-tip {
    margin-top: 12px;
    color: #9ca3af;
    font-size: 12px;
}

/* 表单布局 */
.form-grid {
    display: grid;
    grid-template-columns: 1fr;
    gap: 40px;
}

@media (min-width: 768px) {
    .form-grid {
        grid-template-columns: 1.2fr 0.8fr;
        gap: 60px;
    }
}

.section-title {
    font-size: 16px;
    font-weight: 600;
    color: #374151;
    margin-bottom: 20px;
    padding-left: 12px;
    border-left: 4px solid #10b981;
}

/* 自定义输入框样式 */
.edit-form :deep(.ant-input),
.edit-form :deep(.ant-input-textarea) {
    border-radius: 8px;
    border-color: #e5e7eb;
    padding: 10px 12px;
}

.edit-form :deep(.ant-input):focus,
.edit-form :deep(.ant-input-textarea):focus {
    border-color: #10b981;
    box-shadow: 0 0 0 2px rgba(16, 185, 129, 0.1);
}

.edit-form :deep(.ant-form-item-label > label) {
    color: #4b5563;
    font-weight: 500;
}

/* 只读字段样式 */
.input-with-icon {
    display: flex;
    align-items: center;
    padding: 12px 16px;
    background-color: #f9fafb;
    border: 1px solid #f3f4f6;
    border-radius: 8px;
    color: #374151;
}

.field-icon {
    color: #9ca3af;
    margin-right: 12px;
    font-size: 16px;
}

.field-value {
    flex: 1;
    font-family: monospace;
    font-weight: 500;
}

.field-tag {
    font-size: 12px;
    color: #9ca3af;
    background-color: #f3f4f6;
    padding: 2px 8px;
    border-radius: 4px;
    border: 1px solid #e5e7eb;
}

.action-link {
    padding: 0;
    height: auto;
    color: #10b981;
}

.action-link:hover {
    color: #059669;
}

.form-tip {
    font-size: 12px;
    color: #9ca3af;
    margin-top: 6px;
}

/* 底部按钮 */
.form-footer {
    margin-top: 40px;
    padding-top: 24px;
    border-top: 1px solid #f3f4f6;
    display: flex;
    justify-content: flex-end;
    gap: 16px;
}

.cancel-btn {
    border-radius: 8px;
    color: #6b7280;
    border-color: #d1d5db;
}

.cancel-btn:hover {
    color: #374151;
    border-color: #9ca3af;
}

.save-btn {
    background-color: #10b981; 
    border-color: #10b981;
    border-radius: 8px;
    padding-left: 24px;
    padding-right: 24px;
    font-weight: 500;
}

.save-btn:hover {
    background-color: #059669;
    border-color: #059669;
}

@media (max-width: 640px) {
    .content-card {
        padding: 24px;
    }
    
    .form-footer {
        flex-direction: column-reverse;
    }
    
    .save-btn, .cancel-btn {
        width: 100%;
    }
}
</style>