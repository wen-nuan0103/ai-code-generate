<template>
  <div class="app-card" :class="{ 'app-card--featured': featured }">
    <div class="app-preview">
      <div class="img-wrapper">
        <img v-if="app.cover" :src="app.cover" :alt="app.appName" />
        <div v-else class="app-placeholder">
          <span>🤖</span>
        </div>
      </div>
      
      <div class="app-overlay">
        <a-space>
          <a-button type="primary" shape="round" @click="handleViewChat">开始对话</a-button>
          <a-button v-if="app.deployKey" ghost class="btn-ghost" shape="round" @click="handleViewWork">查看作品</a-button>
        </a-space>
      </div>
    </div>

    <div class="app-info">
      <div class="app-info-left" v-if="!isMyApp">
        <a-avatar :src="app.user?.avatar" :size="40" class="user-avatar">
          {{ app.user?.userName?.charAt(0) || 'U' }}
        </a-avatar>
      </div>
      
      <div class="app-info-right">
        <h3 class="app-title" :title="app.appName">{{ app.appName || '未命名应用' }}</h3>
        
        <div class="app-meta">
          <div class="app-tags" v-if="app.tags && app.tags.length">
             <a-tag 
              v-for="(item, index) in app.tags.slice(0, 3)" 
              :key="index" 
              :bordered="false"
              class="custom-tag"
            >
              {{ item }}
            </a-tag>
          </div>
          
          <div class="app-author-wrapper" v-if="!isMyApp">
            <span class="app-author">
              @{{ app.user?.userName || (featured ? '官方推荐' : '未知用户') }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// Script部分完全保持原样，未做任何修改
interface Props {
  app: API.AppVO
  isMyApp?: boolean
  featured?: boolean
}

interface Emits {
  (e: 'view-chat', appId: string | number | undefined): void
  (e: 'view-work', app: API.AppVO): void
}

const props = withDefaults(defineProps<Props>(), {
  featured: false,
})

const emit = defineEmits<Emits>()

const handleViewChat = () => {
  emit('view-chat', props.app.id)
}

const handleViewWork = () => {
  emit('view-work', props.app)
}
</script>

<style scoped>
/* 容器基础样式 */
.app-card {
  position: relative;
  background: #ffffff;
  border-radius: 16px;
  overflow: hidden;
  /* 更现代、轻柔的阴影 */
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
  border: 1px solid #f0f0f0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  height: 100%; /* 确保在Grid布局中等高 */
  display: flex;
  flex-direction: column;
}

/* 悬停整体效果 */
.app-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  border-color: transparent;
}

/* Featured 特殊样式 */
.app-card--featured {
  border: 1px solid rgba(24, 144, 255, 0.2);
  background: linear-gradient(to bottom right, #ffffff, #f0f7ff);
}

.app-card--featured:hover {
  box-shadow: 0 20px 25px -5px rgba(24, 144, 255, 0.15);
}

/* 图片区域 */
.app-preview {
  height: 160px;
  position: relative;
  overflow: hidden;
  background: #fafafa;
}

/* 图片包装层，用于做缩放效果 */
.img-wrapper {
  width: 100%;
  height: 100%;
  transition: transform 0.5s ease;
}

.app-card:hover .img-wrapper {
  transform: scale(1.05);
}

.app-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 占位符优化：使用渐变背景 */
.app-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  font-size: 48px;
}

/* 遮罩层 */
.app-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(4px); /* 增加毛玻璃效果 */
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.app-card:hover .app-overlay {
  opacity: 1;
}

/* 遮罩层内的 Ghost 按钮样式修正 */
.btn-ghost {
  color: #fff !important;
  border-color: #fff !important;
}

.btn-ghost:hover {
  color: #40a9ff !important;
  border-color: #40a9ff !important;
  background: #fff !important;
}

/* 信息区域布局 */
.app-info {
  padding: 16px;
  display: flex;
  gap: 12px;
  flex: 1;
  background: #fff;
  z-index: 1; /* 确保在背景之上 */
}

.user-avatar {
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.app-info-right {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

/* 标题样式 */
.app-title {
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 8px;
  line-height: 1.4;
  /* 限制为两行，防止过长，比单行截断更友好 */
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 底部元数据区域 */
.app-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 标签样式优化 */
.app-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  height: 24px; /* 限制高度，保持卡片整齐 */
  overflow: hidden;
}

.custom-tag {
  margin-right: 0 !important;
  background: #f3f4f6;
  color: #4b5563;
  border-radius: 4px;
  font-size: 12px;
}

/* 作者信息 */
.app-author-wrapper {
  margin-top: auto;
}

.app-author {
  font-size: 13px;
  color: #9ca3af;
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>