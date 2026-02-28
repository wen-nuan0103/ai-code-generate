<script lang="ts" setup>
import { login } from '@/api/userController'
import { useUserStore } from '@/stores/user'
import { message } from 'ant-design-vue'
import { UserOutlined, LockOutlined, ArrowRightOutlined,WechatOutlined,GoogleOutlined,GithubOutlined } from '@ant-design/icons-vue'
import { reactive } from 'vue'
import { useRouter } from 'vue-router'

const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})

const router = useRouter()
const userStore = useUserStore()

const handleSubmit = async (values: any) => {
  const res = await login(values)
  if (res.data.code === 0 && res.data.data) {
    await userStore.fetchLoginUser()
    message.success('登录成功，欢迎回来！')
    router.push({
      path: '/',
      replace: true,
    })
  } else {
    message.error('登录失败，' + res.data.message)
  }
}

const handleOtherLogin = (type:string) => {
  message.info(`暂未开发 ${type} 登录`)
}
</script>

<template>
  <div id="userLoginPage">
    <div class="background-blobs">
        <div class="blob blob-1"></div>
        <div class="blob blob-2"></div>
    </div>

    <div class="login-container">
      <div class="header-section">
        <h1 class="title">欢迎回来</h1>
        <p class="subtitle">登录 NoCode，继续你的 AI 创作之旅</p>
      </div>

      <a-form
        :model="formState"
        name="basic"
        autocomplete="off"
        @finish="handleSubmit"
        class="login-form"
      >
        <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
          <a-input v-model:value="formState.userAccount" placeholder="请输入账号" size="large">
            <template #prefix>
              <UserOutlined class="input-icon" />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item
          name="userPassword"
          :rules="[
            { required: true, message: '请输入密码' },
            { min: 8, message: '密码不能小于 8 位' },
          ]"
        >
          <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" size="large">
            <template #prefix>
              <LockOutlined class="input-icon" />
            </template>
          </a-input-password>
        </a-form-item>

        <div class="form-actions">
          <a class="forgot-password">忘记密码？</a>
        </div>

        <a-form-item>
          <a-button type="primary" html-type="submit" size="large" class="submit-button">
            登录
            <ArrowRightOutlined />
          </a-button>
        </a-form-item>
      </a-form>

      <div class="footer-links">
        <div class="login-icons">
          <WechatOutlined class="other-login-icon" @click="handleOtherLogin('微信')"/>
          <GithubOutlined class="other-login-icon" @click="handleOtherLogin('GitHub')"/>
          <GoogleOutlined class="other-login-icon" @click="handleOtherLogin('Google')"/>
        </div>
        <span class="register-text">还没有账号？</span>
        <RouterLink to="/user/register" class="register-link">立即注册</RouterLink>
      </div>
    </div>
  </div>
</template>

<style scoped>
#userLoginPage {
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
    pointer-events: none;
}

.blob {
    position: absolute;
    border-radius: 50%;
    filter: blur(80px);
    opacity: 0.5;
    animation: float 12s infinite ease-in-out alternate;
}

.blob-1 {
    width: 60vw;
    height: 60vw;
    max-width: 800px;
    max-height: 800px;
    background: #0ea5e9;
    top: -15%;
    left: -15%;
    opacity: 0.3;
}

.blob-2 {
    width: 50vw;
    height: 50vw;
    max-width: 700px;
    max-height: 700px;
    background: #4f46e5;
    bottom: -15%;
    right: -10%;
    opacity: 0.3;
    animation-delay: -3s;
}

@keyframes float {
    0% { transform: translate(0, 0) rotate(0deg); }
    100% { transform: translate(30px, 20px) rotate(5deg); }
}

.login-container {
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

.login-container:hover {
    transform: translateY(-2px);
}

.header-section {
  text-align: center;
  margin-bottom: 32px;
}

.title {
  font-size: 30px;
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 8px;
  letter-spacing: -0.5px;
}

.subtitle {
  font-size: 15px;
  color: #64748b;
  margin: 0;
}

.login-form :deep(.ant-input-affix-wrapper),
.login-form :deep(.ant-input) {
  border-radius: 12px;
  padding: 12px 16px;
  font-size: 15px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  transition: all 0.3s ease;
}

.login-form :deep(.ant-input-affix-wrapper:hover) {
  border-color: #0f172a;
  background: #fff;
}

.login-form :deep(.ant-input-affix-wrapper-focused) {
  border-color: #0f172a;
  background: #fff;
  box-shadow: 0 0 0 4px rgba(15, 23, 42, 0.1);
}

.input-icon {
  color: #94a3b8;
  font-size: 18px;
  margin-right: 6px;
}

.login-icons{
  margin-bottom: 12px;
}

.other-login-icon{
  color: #94a3b8;
  font-size: 40px;
  margin: 6px;
  cursor: pointer;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 24px;
  margin-top: -8px;
}

.forgot-password {
  color: #64748b;
  font-size: 13px;
  transition: color 0.3s;
}
.forgot-password:hover {
  color: #0f172a;
}

.submit-button {
  width: 100%;
  height: 52px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s ease;
  color: white;
}

.submit-button:hover {
  background: linear-gradient(135deg, #334155 0%, #1e293b 100%);
  transform: translateY(-1px);
  box-shadow: 0 8px 16px -4px rgba(15, 23, 42, 0.3);
}

.footer-links {
  text-align: center;
  margin-top: 24px;
  padding-top: 12px;
  border-top: 1px solid #f1f5f9;
}

.register-text {
  color: #64748b;
  font-size: 14px;
}

.register-link {
  color: #0f172a;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  margin-left: 4px;
}
.register-link:hover {
  text-decoration: underline;
  color: #3b82f6;
}

@media (max-width: 576px) {
  #userLoginPage {
    padding: 100px 20px 20px 20px;
  }
  .login-container {
    padding: 32px 24px;
  }
  .blob {
    opacity: 0.25;
  }
}
</style>