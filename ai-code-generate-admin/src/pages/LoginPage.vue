<template>
    <div class="login-container">
        <div class="login-content">
            <div class="login-header">
                <div class="logo">
                    <svg width="40" height="40" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <rect width="40" height="40" rx="8" fill="#1890FF" />
                        <path d="M20 10L30 25H10L20 10Z" fill="white" />
                    </svg>
                </div>
                <div class="title-group">
                    <h1 class="app-name">零代码生成平台</h1>
                    <span class="app-desc">Low-Code Admin Console</span>
                </div>
            </div>

            <a-card :bordered="false" class="login-card">
                <a-form :model="formState" name="login" :rules="rules" @finish="handleSubmit" layout="vertical">
                    <a-form-item name="userAccount" label="管理员账号">
                        <a-input v-model:value="formState.userAccount" placeholder="admin" size="large">
                            <template #prefix>
                                <UserOutlined style="color: rgba(0,0,0,.25)" />
                            </template>
                        </a-input>
                    </a-form-item>

                    <a-form-item name="userPassword" label="登录密码">
                        <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" size="large">
                            <template #prefix>
                                <LockOutlined style="color: rgba(0,0,0,.25)" />
                            </template>
                        </a-input-password>
                    </a-form-item>

                    <div class="form-actions">
                        <span></span>
                        <!-- <a-checkbox v-model:checked="formState.remember">记住我</a-checkbox> -->
                        <a class="forgot-link">忘记密码？</a>
                    </div>

                    <a-form-item class="submit-item">
                        <a-button type="primary" html-type="submit" size="large" block :loading="loading">
                            登 录
                        </a-button>
                    </a-form-item>
                </a-form>
            </a-card>

            <div class="footer-copyright">
                © 2026 Zero-Code Platform. All Rights Reserved.
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue';
import type { Rule } from 'ant-design-vue/es/form';
import { login } from '@/api/userController';
import { useUserStore } from '@/stores/user';

interface FormState {
    userAccount: string;
    userPassword: string;
    isAdminLogin: boolean;
}

const formState = reactive<FormState>({
    userAccount: '',
    userPassword: '',
    isAdminLogin: true,
});

const router = useRouter()
const userStore = useUserStore()

const loading = ref<boolean>(false);

const rules: Record<string, Rule[]> = {
    userAccount: [
        { required: true, message: '请输入管理员账号', trigger: 'blur' },
        { min: 3, message: '账号长度至少3位', trigger: 'blur' }
    ],
    userPassword: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码长度至少6位', trigger: 'blur' }
    ]
};

const handleSubmit = async (values: FormState) => {
    values = { ...values, isAdminLogin: true }
    loading.value = true;

    try {
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
    } catch (err) {
        message.error('登录服务异常');
    } finally {
        loading.value = false;
    }
};
</script>

<style scoped>
.login-container {
    min-height: 100vh;
    width: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    background-color: #f0f2f5;
    background-image: url('@/assets/background.svg');
    background-repeat: no-repeat;
    background-position: center 110px;
    background-size: 100%;
}

.login-content {
    width: 100%;
    max-width: 550px;
    padding: 0 20px;
}

.login-header {
    text-align: center;
    margin-bottom: 40px;
}

.logo {
    margin-bottom: 16px;
}

.title-group {
    display: flex;
    flex-direction: column;
    align-items: center;
}

.app-name {
    font-size: 28px;
    font-weight: 600;
    color: rgba(0, 0, 0, 0.85);
    margin: 0;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial;
}

.app-desc {
    margin-top: 8px;
    color: rgba(0, 0, 0, 0.45);
    font-size: 14px;
}

.login-card {
    box-shadow: 0 6px 16px -8px rgba(0, 0, 0, 0.08), 0 9px 28px 0 rgba(0, 0, 0, 0.05), 0 12px 48px 16px rgba(0, 0, 0, 0.03);
    border-radius: 8px;
}

.form-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
}

.submit-item {
    margin-bottom: 0;
}

.footer-copyright {
    margin-top: 48px;
    text-align: center;
    color: rgba(0, 0, 0, 0.45);
    font-size: 14px;
}

:where(.css-dev-only-do-not-override-1p3hq3p).ant-input-affix-wrapper-lg {
    font-size: 18px;
}

/* 移动端适配 */
@media (max-width: 576px) {
    .login-container {
        background-color: #fff;
        background-image: none;
    }

    .login-card {
        box-shadow: none;
    }
}
</style>