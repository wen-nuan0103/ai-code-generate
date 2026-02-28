<script lang="ts" setup>
import { register } from '@/api/userController'
import { message } from 'ant-design-vue'
import { UserOutlined, LockOutlined, SafetyOutlined, RocketOutlined,CodepenOutlined } from '@ant-design/icons-vue'
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

const loading = ref(false);

const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
  inviteCode:''
})

const router = useRouter()

const handleSubmit = async (values: any) => {
  loading.value = true;
  try {
    const res = await register(values)
    if (res.data.code === 0 && res.data.data) {
      message.success('注册成功，请登录')
      router.push({
        path: '/user/login',
        replace: true,
      })
    } else {
      message.error('注册失败，' + res.data.message)
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div id="userRegisterPage">
    <div class="background-blobs">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
      <div class="blob blob-3"></div>
    </div>

    <div class="register-container">
      <div class="register-header">
        <h1 class="title">创建账号</h1>
        <p class="subtitle">免费加入 NoCode，体验 AI 生成应用的魅力</p>
      </div>

      <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit" class="register-form">
        <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
          <a-input v-model:value="formState.userAccount" placeholder="设置账号" size="large">
            <template #prefix>
              <UserOutlined class="input-icon" />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item name="userPassword" :rules="[
          { required: true, message: '请输入密码' },
          { min: 8, message: '密码不能小于 8 位' },
        ]">
          <a-input-password v-model:value="formState.userPassword" placeholder="设置密码（至少8位）" size="large">
            <template #prefix>
              <LockOutlined class="input-icon" />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item name="checkPassword" :rules="[
          { required: true, message: '请再次输入密码' },
          { min: 8, message: '密码不能小于 8 位' },
        ]">
          <a-input-password v-model:value="formState.checkPassword" placeholder="确认密码" size="large">
            <template #prefix>
              <SafetyOutlined class="input-icon" />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item name="inviteCode">
          <a-input v-model:value="formState.inviteCode" placeholder="邀请码" size="large">
            <template #prefix>
              <CodepenOutlined class="input-icon" />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item>
          <a-button type="primary" html-type="submit" size="large" class="submit-button" :loading="loading">
            立即注册
            <RocketOutlined />
          </a-button>
        </a-form-item>
      </a-form>

      <div class="footer-links">
        <span class="login-text">已有账号？</span>
        <RouterLink to="/user/login" class="login-link">立即登录</RouterLink>
      </div>
    </div>
  </div>
</template>

<style scoped>
#userRegisterPage {
  min-height: 100vh;
  padding-top: 100px;
  padding-bottom: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background-color: #f8fafc;
}

.background-blobs {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  overflow: hidden;
  pointer-events: none;
}

.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.6;
  animation: float 10s infinite ease-in-out alternate;
}

.blob-1 {
  width: 60vw;
  height: 60vw;
  max-width: 800px;
  max-height: 800px;
  background: #8b5cf6;
  top: -20%;
  left: -10%;
  opacity: 0.35;
}

.blob-2 {
  width: 50vw;
  height: 50vw;
  max-width: 700px;
  max-height: 700px;
  background: #3b82f6;
  bottom: -10%;
  right: -10%;
  opacity: 0.35;
  animation-delay: -5s;
}

.blob-3 {
  width: 300px;
  height: 300px;
  background: #ec4899;
  top: 40%;
  right: 20%;
  opacity: 0.2;
  filter: blur(60px);
  animation-duration: 15s;
}

@keyframes float {
  0% {
    transform: translate(0, 0) scale(1);
  }

  100% {
    transform: translate(20px, 40px) scale(1.05);
  }
}

.register-container {
  width: 100%;
  margin-top: -100px;
  max-width: 580px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 1);
  border-radius: 24px;
  box-shadow:
    0 25px 50px -12px rgba(0, 0, 0, 0.15),
    0 0 0 1px rgba(0, 0, 0, 0.02);
  padding: 48px 40px;
  z-index: 1;
  transition: transform 0.3s ease;
}

.register-container:hover {
  transform: translateY(-2px);
}

.register-header {
  text-align: center;
  margin-bottom: 32px;
}

.title {
  font-size: 30px;
  font-weight: 800;
  color: #1e293b;
  margin-bottom: 8px;
  letter-spacing: -0.5px;
}

.subtitle {
  font-size: 15px;
  color: #64748b;
  margin: 0;
}

.register-form :deep(.ant-input-affix-wrapper),
.register-form :deep(.ant-input) {
  border-radius: 12px;
  padding: 12px 16px;
  font-size: 15px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  transition: all 0.3s ease;
}

.register-form :deep(.ant-input-affix-wrapper:hover) {
  border-color: #3b82f6;
  background: #fff;
}

.register-form :deep(.ant-input-affix-wrapper-focused) {
  border-color: #3b82f6;
  background: #fff;
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1);
}

.register-form :deep(.ant-form-item) {
  margin-bottom: 24px;
}

.input-icon {
  color: #94a3b8;
  font-size: 18px;
  margin-right: 6px;
}

.submit-button {
  width: 100%;
  height: 52px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #3b82f6 0%, #6366f1 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
  transition: all 0.3s ease;
  margin-top: 8px;
  color: white;
}

.submit-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgba(59, 130, 246, 0.4);
  background: linear-gradient(135deg, #2563eb 0%, #4f46e5 100%);
}

.footer-links {
  text-align: center;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #f1f5f9;
}

.login-text {
  color: #64748b;
  font-size: 14px;
}

.login-link {
  color: #3b82f6;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  margin-left: 4px;
}

@media (max-width: 576px) {
  #userRegisterPage {
    padding: 100px 20px 20px 20px;
  }

  .register-container {
    padding: 32px 24px;
  }

  .blob {
    opacity: 0.2;
  }
}
</style>