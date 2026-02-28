<script setup lang="ts">
import { useUserStore } from '@/stores/user'
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { LogoutOutlined,UserOutlined} from '@ant-design/icons-vue'
import { logout } from '@/api/userController'
import { message } from 'ant-design-vue'
import checkAccess from '@/access/checkAccess'

// 菜单配置项
const menuItems = ref([
  { key: '/', label: '使用文档' },
  { key: '/community', label: '交流社区' },
])

const router = useRouter()
const allRoutes = router.getRoutes()
const userStore = useUserStore()

// 滚动状态
const scrolled = ref(false)

// 处理滚动事件
const handleScroll = () => {
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop
  scrolled.value = scrollTop > 50
}

// 处理菜单点击
const handleMenuClick = ({ key }: { key: string }) => {
  router.push(key)
}

// 将菜单项转换为路由菜单
const menuToRouteItem = (menu: any) => {
  const route = allRoutes.find((it) => it.path === menu.key)
  return {
    path: route?.path,
    meta: route?.meta,
  }
}

// 过滤菜单项
const items = computed(() => {
  return menuItems.value.filter((menu) => {
    const item = menuToRouteItem(menu)
    if (item.meta?.hideInMenu) {
      return false
    }
    return checkAccess(userStore.user, item.meta?.access as string)
  })
})

const handleLogout = async () => {
  const result = await logout()
  if (result.data.code === 0) {
    userStore.setUser({
      userName: '未登录',
    })
    message.success('退出登录成功')
    await router.push('/user/login')
  } else {
    message.error(`退出登录失败: ${result.data.message}`)
  }
}

// // 生命周期
// onMounted(() => {
//   window.addEventListener('scroll', handleScroll)
//   handleScroll() // 初始化状态
// })

// onUnmounted(() => {
//   window.removeEventListener('scroll', handleScroll)
// })
</script>

<template>
  <a-layout-header class="header" :class="{ 'header-scrolled': scrolled }">
    <div class="header-content">
      <div class="logo-section" @click="() => router.push('/')">
        <!-- <img src="@/assets/logo.svg" alt="logo" class="logo" /> -->
        <span class="title">NoCode</span>
      </div>
      <a-menu mode="horizontal" :items="items" class="menu" @click="handleMenuClick" />
      <div class="user-section">
        <div class="user-login-status">
          <div v-if="userStore.user.id">
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
          <div v-else>
            <button class="login-btn" @click="router.push('/user/login')">
              <span>登录</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </a-layout-header>
</template>

<style scoped>
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: rgba(248, 249, 245, 0.6);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  box-shadow: none;
  border: 1px solid rgba(255, 255, 255, 0.2);
  z-index: 1000;
  transition: all 0.3s ease;
}

/* .header-scrolled {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(30px);
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.08);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
} */


.header-content {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40px;
  height: 80px;
}

.ant-layout .ant-layout-header {
  height: 80px !important;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
  cursor: pointer;
  transition: all 0.3s;
}

.logo-section:hover {
  opacity: 0.8;
}

.logo {
  height: 36px;
  width: 36px;
  transition: all 0.3s;
}

.title {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  white-space: nowrap;
  letter-spacing: -0.5px;
}

.menu {
  flex: 1;
  min-width: 0;
  border-bottom: none;
  line-height: 64px;
  margin: 0 40px;
  background: transparent;
}

.user-section {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.user-info {
  cursor: pointer;
  transition: all 0.3s;
}

.user-info:hover {
  opacity: 0.8;
}

.user-name {
  color: #1e293b;
  font-weight: 500;
}

.login-btn {
  background-color: black;
  color: white;
  border: none;
  text-align: center;
  padding: 4px 24px;
  width: 85px;
  height: 40px;
  font-weight: 500;
  transition: all 0.3s;
  border-radius: 12px;
  cursor: pointer;
}

.login-btn span {
  font-size: medium;
}

/* .login-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
} */

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    padding: 0 20px;
  }

  .title {
    font-size: 16px;
  }

  .menu {
    margin: 0 16px;
  }

  .logo {
    height: 32px;
    width: 32px;
  }
}

@media (max-width: 576px) {
  .header-content {
    padding: 0 16px;
  }

  .menu {
    flex: 0;
    min-width: auto;
    margin: 0 8px;
  }

  .user-name {
    display: none;
  }
}
</style>
