<script setup lang="ts">
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import {
  EditOutlined,
  CopyOutlined,
  CrownFilled,
  UserOutlined,
  ShareAltOutlined,
  SafetyCertificateOutlined,
  IdcardOutlined,
  CheckCircleFilled
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { computed } from 'vue'

const userStore = useUserStore()
const router = useRouter()
const user = computed(() => userStore.user)

// 复制功能
const handleCopy = (text: string) => {
  if (!text) return
  navigator.clipboard.writeText(text).then(() => {
    message.success('复制成功')
  })
}

// VIP 判断
const isVip = computed(() => {
  return user.value.vipExpireTime && dayjs(user.value.vipExpireTime).isAfter(dayjs())
})
</script>

<template>
  <div class="page-wrapper">
    <main class="main-content">
      
      <div class="profile-header">
        <div class="avatar-container">
          <div class="avatar-ring">
            <a-avatar :src="user.avatar" :size="96" class="user-avatar">
              <template #icon><UserOutlined /></template>
            </a-avatar>
          </div>
        </div>
        
        <h1 class="user-name">
          {{ user.userName || '无名大侠' }}
          <span v-if="isVip" class="vip-badge-icon" title="尊贵会员"><CrownFilled /></span>
        </h1>
        
        <div class="user-stats">
          <span>角色 <strong class="text-dark">{{ user.role === 'admin' ? '管理员' : '普通用户' }}</strong></span>
          <span class="dot"></span>
          <span>积分 <strong class="text-dark">{{ user.points || 0 }}</strong></span>
        </div>

        <p class="user-bio">
          {{ user.profile || '这个人很懒，什么都没有写...' }}
          <a class="edit-icon" @click="router.push('/user/edit')" title="编辑资料">
            <EditOutlined />
          </a>
        </p>
      </div>

      <div class="content-card">
        
        <div class="tabs-nav">
          <div class="tab-item active">
            个人中心
            <span class="badge">Info</span>
          </div>
          <div class="tab-item disabled">我的创作</div>
          <div class="tab-item disabled">收藏夹</div>
        </div>

        <div class="info-grid">
          
          <div class="grid-card">
            <div class="card-image-placeholder bg-blue">
              <IdcardOutlined class="card-icon" />
            </div>
            <div class="card-content">
              <h3 class="card-title">账号信息</h3>
              <div class="card-desc">
                <div class="info-row">
                  <span class="label">账号 ID:</span>
                  <span class="value">{{ user.userAccount }}</span>
                </div>
                <div class="info-row">
                  <span class="label">注册时间:</span>
                  <span class="value">{{ user.createTime ? dayjs(user.createTime).format('YYYY-MM-DD') : '未知' }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="grid-card" :class="{ 'vip-active': isVip }">
            <div class="card-image-placeholder" :class="isVip ? 'bg-gold' : 'bg-gray'">
              <CrownFilled class="card-icon" />
            </div>
            <div class="card-content">
              <h3 class="card-title">
                会员权益
                <CheckCircleFilled v-if="isVip" class="vip-check" />
              </h3>
              <div class="card-desc">
                <div v-if="isVip">
                  <div class="info-row">
                    <span class="label">有效期至:</span>
                    <span class="value highlight">{{ dayjs(user.vipExpireTime).format('YYYY-MM-DD') }}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">状态:</span>
                    <span class="value">生效中</span>
                  </div>
                </div>
                <div v-else>
                  <p class="mb-2">您当前还不是会员</p>
                  <a-button type="primary" size="small" class="action-btn">立即开通</a-button>
                </div>
              </div>
            </div>
          </div>

          <div class="grid-card">
            <div class="card-image-placeholder bg-green">
              <SafetyCertificateOutlined class="card-icon" />
            </div>
            <div class="card-content">
              <h3 class="card-title">安全设置</h3>
              <div class="card-desc">
                <div class="info-row">
                  <span class="label">绑定手机:</span>
                  <span class="value">{{ user.phone || '未绑定' }}</span>
                </div>
                <div class="info-row" style="margin-top: 4px;">
                  <span class="label">密码强度:</span>
                  <span class="value">*********</span>
                </div>
              </div>
            </div>
          </div>

          <div class="grid-card">
            <div class="card-image-placeholder bg-purple">
              <ShareAltOutlined class="card-icon" />
            </div>
            <div class="card-content">
              <h3 class="card-title">邀请有礼</h3>
              <div class="card-desc">
                <p class="text-xs mb-2">分享邀请码获积分奖励</p>
                <div class="share-box" @click="handleCopy(user.shareCode || '')">
                  <span class="code">{{ user.shareCode || '暂无' }}</span>
                  <CopyOutlined class="copy-icon" v-if="user.shareCode" />
                </div>
              </div>
            </div>
          </div>

        </div>

        <div class="footer-hint">
          <span class="text-hint">End of profile</span>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
/* 全局容器 - 对应 HTML body 的渐变背景 */
.page-wrapper {
  /* min-height: 100vh; */
  /* background-image: linear-gradient(to bottom, #f0fdfa, #ffffff); HTML 中的 from-teal-50 */
  background: linear-gradient(to bottom, #f0fdfa, #ffffff);
  color: #1f2937;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  padding-bottom: 80px;
}

.main-content {
  padding-top: 40px;
  padding-left: 16px;
  padding-right: 16px;
  max-width: 1280px; /* max-w-7xl */
  margin: 0 auto;
}

/* 头部样式 */
.profile-header {
  max-width: 768px; /* max-w-3xl */
  margin: 0 auto 48px auto;
  text-align: center;
}

.avatar-container {
  display: inline-block;
  position: relative;
  margin-bottom: 16px;
}

.avatar-ring {
  width: 104px; /* w-24 + padding */
  height: 104px;
  border-radius: 50%;
  padding: 4px;
  background-color: #fff;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  margin: 0 auto;
}

.user-name {
  font-size: 24px;
  font-weight: 700;
  color: #111827; /* text-gray-900 */
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.vip-badge-icon {
  color: #f59e0b;
  font-size: 20px;
}

.user-stats {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  font-size: 14px;
  color: #6b7280; /* text-gray-500 */
  margin-bottom: 16px;
}

.text-dark {
  color: #111827;
}

.dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background-color: #d1d5db;
}

.user-bio {
  color: #4b5563; /* text-gray-600 */
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.edit-icon {
  color: #9ca3af;
  cursor: pointer;
  transition: color 0.2s;
}

.edit-icon:hover {
  color: #10b981; /* primary color */
}

/* 内容卡片样式 */
.content-card {
  background-color: #fff;
  border-radius: 2rem; /* rounded-[2rem] */
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  padding: 40px;
  min-height: 600px;
  border: 1px solid #f3f4f6;
}

/* Tab 导航样式 */
.tabs-nav {
  display: flex;
  justify-content: center;
  border-bottom: 1px solid #f3f4f6;
  margin-bottom: 40px;
  gap: 32px;
}

.tab-item {
  padding: 16px 4px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  color: #6b7280;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
}

.tab-item.active {
  border-color: #10b981; /* primary */
  color: #10b981;
}

.tab-item:hover:not(.active) {
  color: #374151;
  border-color: #d1d5db;
}

.tab-item.disabled {
  opacity: 0.5;
  cursor: default;
}

.badge {
  background-color: #ccfbf1; /* teal-100 */
  color: #0f766e; /* teal-700 */
  padding: 2px 8px;
  border-radius: 9999px;
  font-size: 12px;
}

/* 信息栅格样式 */
.info-grid {
  display: grid;
  grid-template-columns: repeat(1, 1fr);
  gap: 32px;
}

@media (min-width: 768px) {
  .info-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.grid-card {
  position: relative;
  overflow: hidden;
  border-radius: 16px; /* rounded-2xl */
  background-color: #fff;
  border: 1px solid #e5e7eb;
  transition: all 0.3s;
  display: flex;
  align-items: flex-start;
  padding: 24px;
  gap: 20px;
}

.grid-card:hover {
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  transform: translateY(-4px);
}

.card-image-placeholder {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.card-icon {
  font-size: 28px;
  color: #fff;
}

/* 颜色变体 */
.bg-blue { background-color: #3b82f6; }
.bg-gold { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
.bg-gray { background-color: #9ca3af; }
.bg-green { background-color: #10b981; }
.bg-purple { background-color: #8b5cf6; }

.card-content {
  flex: 1;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.card-desc {
  font-size: 14px;
  color: #6b7280;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
  border-bottom: 1px dashed #f3f4f6;
  padding-bottom: 4px;
}

.info-row:last-child {
  border-bottom: none;
}

.label {
  color: #9ca3af;
}

.value {
  color: #4b5563;
  font-weight: 500;
}

.highlight {
  color: #10b981;
  font-weight: 600;
}

/* VIP 特殊样式 */
.vip-active {
  border-color: #fcd34d;
  background: linear-gradient(to right bottom, #fff, #fffbeb);
}

.vip-check {
  color: #f59e0b;
  font-size: 16px;
}

.action-btn {
  background-color: #10b981; /* primary */
  border-color: #10b981;
}

.action-btn:hover {
  background-color: #059669;
  border-color: #059669;
}

/* 邀请码样式 */
.share-box {
  background-color: #f3f4f6;
  padding: 8px 12px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  transition: background 0.2s;
  margin-top: 8px;
}

.share-box:hover {
  background-color: #e5e7eb;
}

.code {
  font-family: monospace;
  font-weight: 600;
  color: #4b5563;
  letter-spacing: 1px;
}

.copy-icon {
  color: #10b981;
}

.footer-hint {
  margin-top: 48px;
  text-align: center;
}

.text-hint {
  color: #9ca3af;
  font-size: 14px;
}

@media (max-width: 640px) {
  .content-card {
    padding: 24px;
  }
  
  .grid-card {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  
  .info-row {
    justify-content: center;
    gap: 8px;
  }
}
</style>