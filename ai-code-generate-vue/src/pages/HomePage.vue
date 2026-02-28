<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useUserStore } from '@/stores/user'
import { addApp, listMyAppVoByPage, listAppVoByPage } from '@/api/appController'
import { getDeployUrl } from '@/config/env'
import AppCard from '@/components/AppCard.vue'

const router = useRouter()
const userStore = useUserStore()

// 用户提示词
const userPrompt = ref('')
const creating = ref(false)

// 我的应用数据
const myApps = ref<API.AppVO[]>([])
const myAppsPage = reactive({
  current: 1,
  pageSize: 3,
  total: 0,
})
// 我的应用排序
const myAppsSortType = ref('default')

// 精选应用数据
const featuredApps = ref<API.AppVO[]>([])
const featuredAppsPage = reactive({
  current: 1,
  pageSize: 3,
  total: 0,
})
// 精选应用排序和标签筛选
const featuredSortType = ref('default')
const selectedTag = ref('全部')

// 示例标签列表
const categoryTags = [
  '全部',
  '工具',
  '网站',
  '数据分析',
  '活动页面',
  '管理平台',
  '用户应用',
  '个人管理',
  '游戏',
]

// 设置提示词
const setPrompt = (prompt: string) => {
  userPrompt.value = prompt
}

// 创建应用
const createApp = async () => {
  if (!userPrompt.value.trim()) {
    message.warning('请输入应用描述')
    return
  }

  if (!userStore.user.id) {
    message.warning('请先登录')
    await router.push('/user/login')
    return
  }

  creating.value = true
  try {
    const res = await addApp({
      initPrompt: userPrompt.value.trim(),
    })

    if (res.data.code === 0 && res.data.data) {
      message.success('应用创建成功')
      const appId = String(res.data.data)
      await router.push(`/app/chat/${appId}`)
    } else {
      message.error('创建失败：' + res.data.message)
    }
  } catch (error) {
    console.error('创建应用失败：', error)
    message.error('创建失败，请重试')
  } finally {
    creating.value = false
  }
}

// 加载我的应用
const loadMyApps = async () => {
  if (!userStore.user.id) {
    return
  }

  try {
    let sortField = 'create_time'
    let sortOrder = 'desc'

    if (myAppsSortType.value === 'newest') {
      sortField = 'create_time'
      sortOrder = 'desc'
    } else if (myAppsSortType.value === 'hottest') {
      sortField = 'view_count'
      sortOrder = 'desc'
    }

    const res = await listMyAppVoByPage({
      current: myAppsPage.current,
      pageSize: myAppsPage.pageSize,
      sortField,
      sortOrder,
    })

    if (res.data.code === 0 && res.data.data) {
      myApps.value = res.data.data.records || []
      myAppsPage.total = Number.parseInt(res.data.data.totalRow as unknown as string) || 0
    }
  } catch (error) {
    console.error('加载我的应用失败：', error)
  }
}

// 切换我的应用排序
const changeMyAppsSort = (sortType: string) => {
  myAppsSortType.value = sortType
  myAppsPage.current = 1
  loadMyApps()
}

// 加载精选应用
const loadFeaturedApps = async () => {
  try {
    let sortField = 'create_time'
    let sortOrder = 'desc'

    if (featuredSortType.value === 'newest') {
      sortField = 'create_time'
      sortOrder = 'desc'
    } else if (featuredSortType.value === 'hottest') {
      sortField = 'view_count'
      sortOrder = 'desc'
    }

    const res = await listAppVoByPage({
      current: featuredAppsPage.current,
      pageSize: featuredAppsPage.pageSize,
      sortField,
      sortOrder,
    })

    if (res.data.code === 0 && res.data.data) {
      featuredApps.value = res.data.data.records || []
      featuredAppsPage.total = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('加载精选应用失败：', error)
  }
}

// 切换精选应用排序
const changeFeaturedSort = (sortType: string) => {
  featuredSortType.value = sortType
  featuredAppsPage.current = 1
  loadFeaturedApps()
}

// 切换标签筛选
const changeTag = (tag: string) => {
  selectedTag.value = tag
  featuredAppsPage.current = 1
  loadFeaturedApps()
}

// 查看对话
const viewChat = (appId: string | number | undefined) => {
  if (appId) {
    router.push(`/app/chat/${appId}?view=1`)
  }
}

// 查看作品
const viewWork = (app: API.AppVO) => {
  if (app.deployKey) {
    const url = getDeployUrl(app.deployKey)
    window.open(url, '_blank')
  }
}

onMounted(() => {
  loadMyApps()
  loadFeaturedApps()
})
</script>

<template>
          <img
      src="@/assets/home-background.png"
      alt="background"
      class="background-image"
      draggable="false"
    />
  <div id="homePage">
    <div class="hero-container">
      
      <div class="floating-icon icon-blue animate-float">
        <svg viewBox="0 0 24 24" fill="currentColor" width="32" height="32">
          <path d="M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm-5 14H4v-4h11v4zm0-5H4V9h11v4zm5 5h-4V9h4v9z"/>
        </svg>
      </div>
      <div class="floating-icon icon-orange animate-float-rev">
        <svg viewBox="0 0 24 24" fill="currentColor" width="28" height="28">
          <path d="M4 8h4V4H4v4zm6 12h4v-4h-4v4zm-6 0h4v-4H4v4zm0-6h4v-4H4v4zm6 0h4v-4h-4v4zm6-10v4h4V4h-4zm-6 4h4V4h-4zm6 6h4v-4h-4v4zm0 6h4v-4h-4v4z"/>
        </svg>
      </div>
      <div class="floating-icon icon-purple animate-float">
        <svg viewBox="0 0 24 24" fill="currentColor" width="24" height="24">
          <path d="M12 3a9 9 0 0 0 0 18c.83 0 1.5-.67 1.5-1.5 0-.39-.15-.74-.39-1.01-.23-.26-.38-.61-.38-.99 0-.83.67-1.5 1.5-1.5H16c2.76 0 5-2.24 5-5 0-4.42-4.03-8-9-8zm-5.5 9c-.83 0-1.5-.67-1.5-1.5S5.67 9 6.5 9 8 9.67 8 10.5 7.33 12 6.5 12zm3-4C8.67 8 8 7.33 8 6.5S8.67 5 9.5 5s1.5.67 1.5 1.5S10.33 8 9.5 8zm5 0c-.83 0-1.5-.67-1.5-1.5S13.67 5 14.5 5s1.5.67 1.5 1.5S15.33 8 14.5 8zm3 4c-.83 0-1.5-.67-1.5-1.5S16.67 9 17.5 9s1.5.67 1.5 1.5-.67 1.5-1.5 1.5z"/>
        </svg>
      </div>
      <div class="floating-icon icon-green animate-float-rev">
        <svg viewBox="0 0 24 24" fill="currentColor" width="20" height="20">
          <path d="M9.4 16.6L4.8 12l4.6-4.6L8 6l-6 6 6 6 1.4-1.4zm5.2 0l4.6-4.6-4.6-4.6L16 6l6 6-6 6-1.4-1.4z"/>
        </svg>
      </div>

      <div class="hero-section">
        <h1 class="hero-title">
          设计，进入自然语言时代
        </h1>
        <p class="hero-description">与 AI 对话轻松创建应用和网站</p>
      </div>

      <div class="input-section-wrapper">
        <div class="glass-panel">
          <div class="input-container">
            <textarea 
              v-model="userPrompt" 
              class="glass-textarea" 
              placeholder="设计点什么？"
            ></textarea>
            
            <div class="input-toolbar">
              <div class="toolbar-actions">
                </div>

              <button 
                class="submit-btn-glass" 
                @click="createApp" 
                :disabled="creating"
              >
                <span v-if="!creating">
                  <svg viewBox="0 0 24 24" fill="currentColor" width="20" height="20"><path d="M4 12l1.41 1.41L11 7.83V20h2V7.83l5.58 5.59L20 12l-8-8-8 8z"/></svg>
                </span>
                <span v-else>...</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <div class="prompt-examples-area">
        <div class="prompt-tags-container">
          <div class="tags-label">
            <svg class="w-4 h-4 text-gray-500" viewBox="0 0 24 24" fill="currentColor" width="16" height="16"><path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg>
            提示词示例
          </div>
          
          <button class="example-tag-btn" @click="setPrompt('创建一个网站的线框图，包含顶部导航、Banner区域、三列内容区和底部Footer。')">
            <svg class="tag-icon" viewBox="0 0 24 24" fill="currentColor" width="16" height="16"><path d="M20 2H4c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zM8 20H4v-4h4v4zm0-6H4v-4h4v4zm0-6H4V4h4v4zm6 12h-4v-4h4v4zm0-6h-4v-4h4v4zm0-6h-4V4h4v4zm6 12h-4v-4h4v4zm0-6h-4v-4h4v4zm0-6h-4V4h4v4z"/></svg>
            线框图
          </button>
          
          <button class="example-tag-btn" @click="setPrompt('设计一个移动端APP界面，包含底部导航栏、顶部搜索框和中间的卡片式列表布局。')">
            <svg class="tag-icon" viewBox="0 0 24 24" fill="currentColor" width="16" height="16"><path d="M17 1.01L7 1c-1.1 0-2 .9-2 2v18c0 1.1.9 2 2 2h10c1.1 0 2-.9 2-2V3c0-1.1-.9-1.99-2-1.99zM17 19H7V5h10v14z"/></svg>
            移动端
          </button>
          
          <button class="example-tag-btn" @click="setPrompt('构建一个响应式网页端布局，左侧为侧边栏菜单，右侧为内容区域，包含数据图表和表格。')">
            <svg class="tag-icon" viewBox="0 0 24 24" fill="currentColor" width="16" height="16"><path d="M20 18c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2H4c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2H0v2h24v-2h-4zM4 6h16v10H4V6z"/></svg>
            网页端
          </button>
          
          <button class="example-tag-btn" @click="setPrompt('制作一个高保真原型图，模拟用户点击购买按钮后的支付弹窗流程。')">
            <svg class="tag-icon" viewBox="0 0 24 24" fill="currentColor" width="16" height="16"><path d="M11.99 18.54l-7.37-5.73L3 14.07l9 7 9-7-1.63-1.27-7.38 5.74zM12 16l7.36-5.73L21 9l-9-7-9 7 1.63 1.27L12 16z"/></svg>
            原型图
          </button>
        </div>

        <div class="prompt-list-container">
          <div class="list-item" @click="setPrompt('一个带有暗色主题、项目卡片和联系表单的开发者作品集。')">
            <div class="item-content">
              <div class="item-icon-box bg-black">
                <svg class="text-white" viewBox="0 0 24 24" fill="currentColor" width="20" height="20"><path d="M3 13h8V3H3v10zm0 8h8v-6H3v6zm10 0h8V11h-8v10zm0-18v6h8V3h-8z"/></svg>
              </div>
              <span class="item-text">一个带有暗色主题、项目卡片和联系表单的开发者作品集。</span>
            </div>
          </div>

          <div class="list-item" @click="setPrompt('设计一款设备跟踪应用程序的现代 SaaS 登陆页面。')">
            <div class="item-content">
              <div class="item-icon-box bg-black">
                <svg class="text-white" viewBox="0 0 24 24" fill="currentColor" width="20" height="20"><path d="M6 2c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2H6zm10 14H8v-2h8v2zm0-4H8v-2h8v2zm0-4H8V6h8v2z"/></svg>
              </div>
              <span class="item-text">设计一款设备跟踪应用程序的现代 SaaS 登陆页面。</span>
            </div>
          </div>

          <div class="list-item" @click="setPrompt('一个护肤品牌电子商务首页，包含产品展示、购物车和结账流程。')">
            <div class="item-content">
              <div class="item-icon-box bg-black">
                <svg class="text-white" viewBox="0 0 24 24" fill="currentColor" width="20" height="20"><path d="M18 6h-2c0-2.21-1.79-4-4-4S8 3.79 8 6H6c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2zm-6-2c1.1 0 2 .9 2 2h-4c0-1.1.9-2 2-2zm6 16H6V8h2v2c0 .55.45 1 1 1s1-.45 1-1V8h4v2c0 .55.45 1 1 1s1-.45 1-1V8h2v12z"/></svg>
              </div>
              <span class="item-text">一个护肤品牌电子商务首页。</span>
            </div>
          </div>
        </div>
      </div>
      </div>

    <div class="content-container">
      <div class="works-container">
        <div class="section" v-if="userStore.user.id">
          <div class="section-header">
            <h2 class="section-title">我的作品</h2>
          </div>
          <div class="app-grid">
            <AppCard
              v-for="app in myApps"
              :key="app.id"
              :app="app"
              @view-chat="viewChat"
              @view-work="viewWork"
              :is-my-app="true"
            />
          </div>
          <div class="pagination-wrapper" v-if="myAppsPage.total > myAppsPage.pageSize">
            <a-pagination
              v-model:current=myAppsPage.current
              v-model:page-size=myAppsPage.pageSize
              :total=myAppsPage.total
              :show-size-changer="false"
              :show-total="(total: number) => `共 ${total} 个应用`"
              @change="loadMyApps"
            />
          </div>
        </div>

        <div class="section">
          <h2 class="section-title">案例广场</h2>

          <div class="filter-bar">
            <a-dropdown>
              <a-button class="sort-dropdown">
                默认排序 <span style="margin-left: 4px">▼</span>
              </a-button>
              <template #overlay>
                <a-menu @click="({ key }: any) => changeFeaturedSort(key)">
                  <a-menu-item key="default">默认排序</a-menu-item>
                  <a-menu-item key="newest">最新</a-menu-item>
                  <a-menu-item key="hottest">最热</a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>

            <div class="tags">
              <a-button
                v-for="tag in categoryTags"
                :key="tag"
                :type="selectedTag === tag ? 'primary' : 'default'"
                @click="changeTag(tag)"
                class="tag-btn"
              >
                {{ tag }}
              </a-button>
            </div>

            <a-button class="view-all-btn" @click="() => router.push('/apps')">
              🔍 全部案例 >
            </a-button>
          </div>

          <div class="featured-grid">
            <AppCard
              v-for="app in featuredApps"
              :key="app.id"
              :app="app"
              :featured="true"
              @view-chat="viewChat"
              @view-work="viewWork"
            />
          </div>
          <div class="more-cases">
            <a-button size="large" @click="() => router.push('/apps')"> 查看更多 </a-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
#homePage {
  width: 100%;
  min-height: 100vh;
  position: relative;
  overflow: hidden;

}

.background-image {
  position: absolute;
  left: 50%;
  top: 80px;
  transform: translateX(-50%);
  width: 2560px;
  min-width: 0;
  max-width: none;
  object-fit: cover;
  object-position: center;
  pointer-events: none;
  user-select: none;
  z-index: 0;
}

.hero-container {
  width: 100%;
  padding: 245px 20px 40px;
  position: relative;
  z-index: 1;
}

.hero-section {
  text-align: center;
  margin-bottom: 40px; 
  color: #1e293b;
}

.hero-title {
  font-size: 3rem;
  font-weight: 700;
  margin: 0 0 20px;
  line-height: 1.2;
  color: #1e293b;
  letter-spacing: -0.025em; 
  position: relative;
  z-index: 2;
}

@media (min-width: 768px) {
  .hero-title {
    font-size: 3rem; 
  }
}

.hero-description {
  font-size: 20px;
  margin: 0;
  opacity: 0.8;
  color: #64748b;
  position: relative;
  z-index: 2;
}

/* 输入框区域 */
.input-section-wrapper {
  position: relative;
  margin: 0 auto;
  max-width: 960px;
  width: 100%;
}

.glass-panel {
  background-color: rgba(255, 255, 255, 0.6); 
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 1.5rem;
  padding: 0.5rem;
  box-shadow: 0 20px 40px -10px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.glass-panel:focus-within {
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.input-container {
  position: relative;
  display: flex;
  flex-direction: column;
}

.glass-textarea {
  width: 100%;
  background: transparent;
  border: none;
  font-size: 1.125rem; 
  color: #1f2937;
  padding: 1rem; 
  min-height: 120px; 
  resize: none;
  outline: none;
  font-family: inherit;
}

.glass-textarea::placeholder {
  color: #9ca3af;
}

.input-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 0.5rem 0.5rem 0.5rem; /* px-2 pb-2 */
  margin-top: 0.5rem; /* mt-2 */
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  overflow-x: auto;
  scrollbar-width: none;
}
.toolbar-actions::-webkit-scrollbar {
  display: none;
}

.submit-btn-glass {
  background-color: #000000; /* bg-black */
  color: #ffffff;
  border-radius: 9999px;
  width: 2.5rem; /* w-10 */
  height: 2.5rem; /* h-10 */
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  cursor: pointer;
  transition: background-color 0.2s;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
}

.submit-btn-glass:hover:not(:disabled) {
  background-color: #1f2937; /* hover:bg-gray-800 */
}

.submit-btn-glass:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

/* ================= 新增：提示词示例与列表区域 ================= */
.prompt-examples-area {
  max-width: 896px;
  margin: 40px auto 0;
  width: 100%;
}

/* 1. 顶部标签行 */
.prompt-tags-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 40px;
}

.tags-label {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #6b7280; /* text-gray-500 */
  font-size: 0.875rem;
  font-weight: 500;
  margin-right: 8px;
}

.example-tag-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 9999px;
  background-color: rgba(255, 255, 255, 0.5); /* bg-white/50 */
  border: 1px solid rgba(255, 255, 255, 0.4);
  color: #374151; /* text-gray-700 */
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
}

.example-tag-btn:hover {
  background-color: #ffffff;
  border-color: #ffffff;
  transform: translateY(-1px);
}

.tag-icon {
  color: #9ca3af; /* text-gray-400 */
}

/* 2. 列表式示例 */
.prompt-list-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.list-item {
  width: 100%;
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: background-color 0.2s;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.list-item:hover {
  background-color: rgba(255, 255, 255, 0.6);
}

.list-item.highlighted {
  background-color: rgba(255, 255, 255, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
}

.list-item.highlighted:hover {
  background-color: rgba(255, 255, 255, 0.8);
}

.item-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.item-icon-box {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.item-icon-box.bg-gray {
  background-color: rgba(229, 231, 235, 0.5); /* bg-gray-200/50 */
}

.list-item:hover .item-icon-box.bg-gray {
  background-color: #ffffff;
}

.item-icon-box.bg-black {
  background-color: #000000;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.text-gray-500 {
  color: #6b7280;
}

.text-white {
  color: #ffffff;
}

.item-text {
  color: #4b5563; /* text-gray-600 */
  font-size: 0.875rem;
  font-weight: 500;
}

.item-text-bold {
  color: #1f2937; /* text-gray-800 */
  font-size: 0.875rem;
  font-weight: 600;
}

/* ================= 浮动图标系统 ================= */
.floating-icon {
  position: absolute;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 1rem;
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.07);
  z-index: 0;
  pointer-events: none;
}

.icon-blue {
  width: 64px;
  height: 64px;
  background-color: rgba(219, 234, 254, 0.5);
  color: #3b82f6;
  top: 40px;
  left: 5%;
}

.icon-orange {
  width: 56px;
  height: 56px;
  background-color: rgba(255, 237, 213, 0.5);
  color: #f97316;
  top: 100px;
  right: 5%;
}

.icon-purple {
  width: 48px;
  height: 48px;
  background-color: rgba(243, 232, 255, 0.5);
  color: #a855f7;
  top: 320px; 
  left: 0;
  margin-left: 20px;
}

.icon-green {
  width: 40px;
  height: 40px;
  background-color: rgba(220, 252, 231, 0.5);
  color: #22c55e;
  top: 60px;
  right: 25%;
}

@media (max-width: 768px) {
  .icon-blue, .icon-orange {
    display: none;
  }
  .icon-green {
    top: 20px;
    right: 10%;
  }
  .icon-purple {
    top: 280px;
    left: -10px;
  }
}

/* 浮动动画 */
@keyframes float {
  0% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-15px) rotate(2deg); }
  100% { transform: translateY(0px) rotate(0deg); }
}

@keyframes float-reverse {
  0% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(15px) rotate(-2deg); }
  100% { transform: translateY(0px) rotate(0deg); }
}

.animate-float {
  animation: float 6s ease-in-out infinite;
}

.animate-float-rev {
  animation: float-reverse 7s ease-in-out infinite;
}

/* ================= 保持原有的业务样式 ================= */

.content-container {
  width: 100%;
  position: relative;
  z-index: 1;
  padding: 60px 40px 80px;
}

.works-container {
  max-width: 1400px;
  margin: 0 auto;
  background: #ffffff;
  border-radius: 24px;
  padding: 40px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
}

.section:not(:last-child) {
  padding-bottom: 60px;
  border-bottom: 1px solid #f1f5f9;
  margin-bottom: 60px;
}

.section-header {
  margin-bottom: 32px;
}

.section-title {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 32px 0;
  color: #1e293b;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;
  flex-wrap: wrap;
}

.sort-dropdown {
  flex-shrink: 0;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  padding: 6px 16px;
  height: auto;
}

.tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  flex: 1;
  min-width: 0;
}

.tag-btn {
  border-radius: 16px;
  padding: 6px 16px;
  height: auto;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 14px;
}

.tags .ant-btn-primary {
  color: #1677ff !important;
}

.tag-btn:hover {
  border-color: #3b82f6;
  color: #3b82f6;
  background: #eff6ff;
}

.view-all-btn {
  flex-shrink: 0;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  padding: 6px 16px;
  height: auto;
  font-weight: 500;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.view-all-btn:hover {
  border-color: #3b82f6;
  color: #3b82f6;
  background: #eff6ff;
}

.app-grid, .featured-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-bottom: 32px;
}

.more-cases {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

.more-cases .ant-btn {
  border-radius: 20px;
  padding: 10px 40px;
  height: auto;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 500;
}

.more-cases .ant-btn:hover {
  border-color: #3b82f6;
  color: #3b82f6;
  background: #eff6ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

@media (max-width: 1024px) {
  .app-grid,
  .featured-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .content-container {
    padding: 40px 24px 60px;
  }

  .works-container {
    padding: 40px 32px;
  }
}

@media (max-width: 768px) {
  .hero-container {
    padding: 80px 16px 32px;
  }

  .app-grid,
  .featured-grid {
    grid-template-columns: 1fr;
  }

  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .tags {
    order: 2;
  }

  .view-all-btn {
    order: 3;
  }

  .sort-dropdown {
    order: 1;
  }

  .works-container {
    padding: 40px 24px;
    border-radius: 16px;
  }
  
  .prompt-tags-container {
    justify-content: flex-start;
    overflow-x: auto;
    padding-bottom: 10px;
    flex-wrap: nowrap;
  }
}

@media (max-width: 576px) {
  .input-section-wrapper, .prompt-examples-area {
    max-width: 100%;
  }

  .works-container {
    padding: 32px 20px;
  }
}
</style>