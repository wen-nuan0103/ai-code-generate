<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'

const route = useRoute()

// 根据路由 meta 判断是否隐藏布局
const hideLayout = computed(() => route.meta.hideLayout)
const hideFooter = computed(() => route.meta.hideFooter)
const hideHeader = computed(() => route.meta.hideHeader)
</script>

<template>
  <a-layout class="basic-layout">
    <!-- 顶部导航栏 -->
    <GlobalHeader v-if="!hideLayout && !hideHeader" />

    <!-- 中间内容区域 -->
    <a-layout-content class="content" :class="{ 'no-padding': hideLayout || hideHeader }">
      <div class="content-wrapper" :class="{ 'full-height': hideLayout }">
        <RouterView />
      </div>
    </a-layout-content>

    <!-- 底部版权信息 -->
    <GlobalFooter v-if="!hideLayout && !hideFooter" />
  </a-layout>
</template>

<style scoped>
.basic-layout {
  background: none;
  min-height: 100vh;
}

.content {
  width: 100%;
  padding: 0;
  background: none;
  margin: 0;
  padding-top: 64px;
  /* 为固定的导航头留出空间 */
}

.content.no-padding {
  padding-top: 0;
  /* 隐藏布局时不需要为导航栏留空间 */
}

.content-wrapper {
  min-height: calc(100vh - 64px);
}

.content-wrapper.full-height {
  min-height: 100vh;
  /* 隐藏布局时占据全屏 */
  height: 100vh;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .content-wrapper {
    padding: 16px;
  }

  .content-wrapper.full-height {
    padding: 0;
    /* 全屏模式不需要内边距 */
  }
}

@media (max-width: 576px) {
  .content-wrapper {
    padding: 12px;
  }

  .content-wrapper.full-height {
    padding: 0;
    /* 全屏模式不需要内边距 */
  }
}
</style>
