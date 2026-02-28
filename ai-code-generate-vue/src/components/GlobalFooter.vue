<script setup lang="ts">
import { ref } from 'vue'

// 底部导航链接
const footerLinks = ref({
  product: [
    { label: '产品功能', url: '#' },
    { label: '使用案例', url: '#' },
    { label: '定价方案', url: '#' },
    { label: '更新日志', url: '#' },
  ],
  resources: [
    { label: '使用文档', url: '#' },
    { label: '开发指南', url: '#' },
    { label: 'API 文档', url: '#' },
    { label: '视频教程', url: '#' },
  ],
  community: [
    { label: '交流社区', url: '#' },
    { label: '用户案例', url: '#' },
    { label: '合作伙伴', url: '#' },
    { label: '开发者计划', url: '#' },
  ],
  support: [
    { label: '帮助中心', url: '#' },
    { label: '联系我们', url: '#' },
    { label: '反馈建议', url: '#' },
    { label: '服务条款', url: '#' },
  ],
})

const socialLinks = ref([
  { name: 'GitHub', icon: '🔗', url: '#' },
  { name: 'Twitter', icon: '🐦', url: '#' },
  { name: 'Discord', icon: '💬', url: '#' },
  { name: 'Email', icon: '📧', url: '#' },
])
</script>

<template>
  <a-layout-footer class="footer">
    <div class="footer-container">
      <!-- 主要内容区域 -->
      <div class="footer-main">
        <!-- Logo 和简介 -->
        <div class="footer-section footer-about">
          <div class="footer-logo">
            <img src="@/assets/logo.svg" alt="NoCode" class="logo" />
            <span class="logo-text">NoCode</span>
          </div>
          <p class="footer-description">AI 驱动的零代码平台，让每个人都能轻松创建应用和网站。</p>
          <div class="social-links">
            <a
              v-for="social in socialLinks"
              :key="social.name"
              :href="social.url"
              :title="social.name"
              class="social-link"
              target="_blank"
              rel="noopener noreferrer"
            >
              {{ social.icon }}
            </a>
          </div>
        </div>

        <!-- 产品 -->
        <div class="footer-section">
          <h3 class="footer-title">产品</h3>
          <ul class="footer-links">
            <li v-for="link in footerLinks.product" :key="link.label">
              <a :href="link.url">{{ link.label }}</a>
            </li>
          </ul>
        </div>

        <!-- 资源 -->
        <div class="footer-section">
          <h3 class="footer-title">资源</h3>
          <ul class="footer-links">
            <li v-for="link in footerLinks.resources" :key="link.label">
              <a :href="link.url">{{ link.label }}</a>
            </li>
          </ul>
        </div>

        <!-- 社区 -->
        <div class="footer-section">
          <h3 class="footer-title">社区</h3>
          <ul class="footer-links">
            <li v-for="link in footerLinks.community" :key="link.label">
              <a :href="link.url">{{ link.label }}</a>
            </li>
          </ul>
        </div>

        <!-- 支持 -->
        <div class="footer-section">
          <h3 class="footer-title">支持</h3>
          <ul class="footer-links">
            <li v-for="link in footerLinks.support" :key="link.label">
              <a :href="link.url">{{ link.label }}</a>
            </li>
          </ul>
        </div>
      </div>

      <!-- 底部版权信息 -->
      <div class="footer-bottom">
        <div class="footer-copyright">
          <p>©2025 小菜版权所有</p>
          <div class="footer-meta">
            <a href="#" target="_blank" rel="noopener noreferrer"> 京ICP备15052537号-11 </a>
            <span class="separator">|</span>
            <a href="#" target="_blank" rel="noopener noreferrer">
              <span class="badge">🛡️</span> 京公网安备11010502056975号
            </a>
            <span class="separator">|</span>
            <span></span>
          </div>
        </div>
      </div>
    </div>
  </a-layout-footer>
</template>

<style scoped>
.footer {
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  padding: 60px 0 0 0;
  /* border-top: 1px solid #e2e8f0; */
  /* margin-top: 80px; */
  z-index: 100;
}

.footer-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 40px;
}

.footer-main {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr 1fr;
  gap: 48px;
  padding-bottom: 48px;
  border-bottom: 1px solid #e2e8f0;
}

.footer-section {
  display: flex;
  flex-direction: column;
}

.footer-about {
  max-width: 320px;
}

.footer-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.logo {
  height: 40px;
  width: 40px;
}

.logo-text {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  letter-spacing: -0.5px;
}

.footer-description {
  color: #64748b;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 20px;
}

.social-links {
  display: flex;
  gap: 12px;
}

.social-link {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: rgba(59, 130, 246, 0.1);
  transition: all 0.3s;
  font-size: 18px;
  text-decoration: none;
}

.social-link:hover {
  background: rgba(59, 130, 246, 0.2);
  transform: translateY(-2px);
}

.footer-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 16px;
}

.footer-links {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.footer-links li a {
  color: #64748b;
  font-size: 14px;
  text-decoration: none;
  transition: all 0.3s;
  display: inline-block;
}

.footer-links li a:hover {
  color: #3b82f6;
  transform: translateX(4px);
}

.footer-bottom {
  padding: 24px 0;
}

.footer-copyright {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.footer-copyright p {
  color: #64748b;
  font-size: 14px;
  margin: 0;
}

.footer-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
}

.footer-meta a {
  color: #64748b;
  font-size: 13px;
  text-decoration: none;
  transition: all 0.3s;
}

.footer-meta a:hover {
  color: #3b82f6;
}

.separator {
  color: #cbd5e1;
}

.badge {
  font-size: 12px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .footer-main {
    grid-template-columns: 2fr 1fr 1fr;
    gap: 32px;
  }

  .footer-section:nth-child(4),
  .footer-section:nth-child(5) {
    grid-column: span 1;
  }
}

@media (max-width: 768px) {
  .footer {
    padding: 40px 0 0 0;
    margin-top: 60px;
  }

  .footer-container {
    padding: 0 20px;
  }

  .footer-main {
    grid-template-columns: 1fr 1fr;
    gap: 32px;
    padding-bottom: 32px;
  }

  .footer-about {
    grid-column: span 2;
    max-width: 100%;
  }
}

@media (max-width: 576px) {
  .footer-main {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .footer-about {
    grid-column: span 1;
  }

  .footer-meta {
    font-size: 12px;
    gap: 8px;
  }

  .social-links {
    justify-content: center;
  }
}
</style>
