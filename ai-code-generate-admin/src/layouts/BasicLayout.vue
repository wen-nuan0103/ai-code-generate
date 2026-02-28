<template>
    <a-layout class="layout-container">
        <a-layout-sider v-model:collapsed="collapsed" :trigger="null" collapsible>
            <div class="logo">
                <span v-if="!collapsed" class="logo-text">Admin Pro</span>
            </div>

            <a-menu v-model:selectedKeys="selectedKeys" v-model:openKeys="openKeys" theme="dark" mode="inline"
                @click="handleMenuClick">
                <template v-for="item in menuList" :key="item.key">

                    <template v-if="item.children && item.children.length > 0">
                        <a-sub-menu :key="item.key">
                            <template #title>
                                <span>
                                    <component :is="item.icon" v-if="item.icon" />
                                    <span>{{ item.title }}</span>
                                </span>
                            </template>
                            <a-menu-item v-for="subItem in item.children" :key="subItem.key">
                                <component :is="subItem.icon" v-if="subItem.icon" />
                                <span>{{ subItem.title }}</span>
                            </a-menu-item>
                        </a-sub-menu>
                    </template>

                    <template v-else>
                        <a-menu-item :key="item.key">
                            <component :is="item.icon" v-if="item.icon" />
                            <span>{{ item.title }}</span>
                        </a-menu-item>
                    </template>

                </template>
            </a-menu>
        </a-layout-sider>

        <a-layout>
            <a-layout-header style="background: #fff; padding: 0">
                <menu-unfold-outlined v-if="collapsed" class="trigger" @click="() => (collapsed = !collapsed)" />
                <menu-fold-outlined v-else class="trigger" @click="() => (collapsed = !collapsed)" />
                <!-- <span style="float: right; margin-right: 20px;">管理员</span> -->
                <div style="float: right; margin-right: 20px;">
                    <a-dropdown>
                        <a-space class="user-info">
                            <a-avatar :src="userStore.user.avatar" style="width: 48px;height: 48px;" />
                            <span class="user-name">{{ userStore.user.userName ?? '无名' }}</span>
                        </a-space>
                        <template #overlay>
                            <a-menu>
                                <a-menu-item @click="router.push('/user/profile')">
                                    <UserOutlined />
                                    个人中心
                                </a-menu-item>
                                <a-menu-item @click="handleLogout">
                                    <LogoutOutlined />
                                    退出登录
                                </a-menu-item>
                            </a-menu>
                        </template>
                    </a-dropdown>
                </div>
            </a-layout-header>

            <a-breadcrumb style="margin: 16px">
                <a-breadcrumb-item v-for="(item, index) in breadList" :key="index">
                    {{ item }}
                </a-breadcrumb-item>
            </a-breadcrumb>

            <a-layout-content
                :style="{ margin: '0 16px 24px 16px', padding: '24px', background: '#fff', minHeight: '280px' }">
                <RouterView />
            </a-layout-content>
        </a-layout>
    </a-layout>
</template>

<script lang="ts" setup>
import { ref, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import {
    UserOutlined,
    MenuUnfoldOutlined,
    MenuFoldOutlined,
    LogoutOutlined
} from '@ant-design/icons-vue';
import { useUserStore } from '@/stores/user'
import { logout } from '@/api/userController';
import { message } from 'ant-design-vue';
import { menuList, type MenuItem } from '@/config/menu';

const collapsed = ref<boolean>(false);
const selectedKeys = ref<string[]>([]);
const openKeys = ref<string[]>([]);
const breadList = ref<string[]>([]);

const router = useRouter();
const route = useRoute();
const userStore = useUserStore()

const handleMenuClick = ({ key }: { key: string }) => {
    router.push(key);
};

const handleLogout = async () => {
    const result = await logout()
    if (result.data.code === 0) {
        userStore.setUser({
            userName: '未登录',
        })
        message.success('退出登录成功')
        await router.push('/login')
    } else {
        message.error(`退出登录失败: ${result.data.message}`)
    }
}

const getBreadcrumbByPath = (menus: MenuItem[], path: string): string[] => {
    for (const item of menus) {
        if (item.key === path) {
            return [item.title];
        }

        if (item.children) {
            const result = getBreadcrumbByPath(item.children, path);
            if (result.length > 0) {
                return [item.title, ...result];
            }
        }
    }
    return [];
};

// 核心：监听路由变化，统一处理高亮、展开和面包屑
watch(
    () => route.path,
    (newPath) => {
        selectedKeys.value = [newPath];
        const parentPath = '/' + newPath.split('/')[1];
        if (newPath.split('/').length > 2 && !openKeys.value.includes(parentPath)) {
            openKeys.value.push(parentPath);
        }
        const matchedBreadcrumbs = getBreadcrumbByPath(menuList, newPath);
        if (matchedBreadcrumbs.length > 0) {
            breadList.value = matchedBreadcrumbs;
        } else {
            if (route.meta?.title) {
                breadList.value = [route.meta.title as string];
            } else {
                breadList.value = newPath
                    .split('/')
                    .filter(item => item)
                    .map(item => item.charAt(0).toUpperCase() + item.slice(1));
            }
        }
    },
    { immediate: true }
);
</script>

<style scoped>
.layout-container {
    height: 100vh;
    /* 让布局撑满全屏 */
}

.trigger {
    font-size: 18px;
    line-height: 64px;
    padding: 0 24px;
    cursor: pointer;
    transition: color 0.3s;
}

.trigger:hover {
    color: #1890ff;
}

.logo {
    height: 32px;
    background: rgba(255, 255, 255, 0.2);
    margin: 16px;
    display: flex;
    justify-content: center;
    align-items: center;
    color: white;
    font-weight: bold;
    overflow: hidden;
}

.logo-text {
    white-space: nowrap;
}
</style>