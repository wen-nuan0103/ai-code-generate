<template>
  <a-modal v-model:open="visible" title="应用详情" :footer="null" width="560px" class="app-detail-modal">
    <div class="app-detail-content">
      <div class="header-section">
        <div class="cover-wrapper" v-if="app?.cover">
          <img :src="app.cover" alt="cover" class="app-cover" />
        </div>
        <div class="header-info">
          <h3 class="app-title">{{ app?.appName || '未命名应用' }}</h3>
          <!-- <div class="app-id-tag">ID: {{ app?.id }}</div> -->
        </div>
      </div>

      <a-descriptions bordered size="small" :column="1" class="detail-descriptions">
        <a-descriptions-item label="创建者">
          <UserInfo :user="app?.user" size="small" />
        </a-descriptions-item>

        <a-descriptions-item label="创建时间">
          {{ formatTime(app?.createTime) }}
        </a-descriptions-item>

        <a-descriptions-item label="更新时间">
          {{ formatTime(app?.updateTime) }}
        </a-descriptions-item>

        <!-- <a-descriptions-item label="审核状态">
          <a-badge :status="getReviewStatus(app?.reviewStatus).status as any"
            :text="getReviewStatus(app?.reviewStatus).text" />
        </a-descriptions-item> -->
      </a-descriptions>

      <!-- <div v-if="showActions" class="app-actions">
        <a-space size="middle" style="width: 100%; justify-content: flex-end">
          <a-button @click="handleEdit" class="action-btn">
            <template #icon>
              <EditOutlined />
            </template>
            编辑配置
          </a-button>

          <a-popconfirm title="确定要删除这个应用吗？" description="删除后无法恢复，请谨慎操作。" @confirm="handleDelete" ok-text="确定删除"
            cancel-text="取消" ok-type="danger">
            <a-button danger class="action-btn">
              <template #icon>
                <DeleteOutlined />
              </template>
              删除应用
            </a-button>
          </a-popconfirm>
        </a-space>
      </div> -->
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { EditOutlined, DeleteOutlined, MessageOutlined } from '@ant-design/icons-vue'
import UserInfo from './UserInfo.vue'
import { formatTime } from '@/utils/time'

interface Props {
  open: boolean
  app?: API.AppVO
  showActions?: boolean
}

interface Emits {
  (e: 'update:open', value: boolean): void
  (e: 'edit'): void
  (e: 'delete'): void
}

const props = withDefaults(defineProps<Props>(), {
  showActions: false,
})

const emit = defineEmits<Emits>()
const router = useRouter()

const visible = computed({
  get: () => props.open,
  set: (value) => emit('update:open', value),
})

// 解析审核状态 (假设后端有 reviewStatus 字段: 0-待审核, 1-通过, 2-拒绝)
const getReviewStatus = (status?: number) => {
  switch (status) {
    case 1:
      return { status: 'success', text: '已发布/审核通过' }
    case 2:
      return { status: 'error', text: '审核未通过' }
    default:
      return { status: 'processing', text: '待审核/私有' }
  }
}

// 跳转到对话
const handleGoToChat = () => {
  if (props.app?.id) {
    router.push(`/app/chat/${props.app.id}`)
    emit('update:open', false) // 关闭弹窗
  }
}

const handleEdit = () => {
  emit('edit')
}

const handleDelete = () => {
  emit('delete')
}
</script>

<style scoped>
.app-detail-content {
  padding: 0 4px;
}

/* 顶部区域样式 */
.header-section {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.cover-wrapper {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #f0f0f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  flex-shrink: 0;
}

.app-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.header-info {
  flex: 1;
}

.app-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
  line-height: 1.4;
}

.app-id-tag {
  display: inline-block;
  background-color: #f3f4f6;
  color: #6b7280;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  font-family: monospace;
}

/* Descriptions 组件样式覆盖 */
.detail-descriptions {
  margin-bottom: 24px;
}

:deep(.ant-descriptions-item-label) {
  width: 100px;
  color: #64748b;
}

/* 底部操作栏 */
.app-actions {
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.action-btn {
  border-radius: 8px;
}
</style>
