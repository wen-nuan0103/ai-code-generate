<template>
  <div id="appChatPage">
    <div class="custom-header">
      <div class="header-left">
        <a-avatar :src="userStore.user.avatar" :size="36" />
        <div class="project-name-container">
          <span class="project-name">{{ appInfo?.appName || '未命名项目' }}</span>
          <a-tag v-if="appInfo?.codeGeneratorType" color="blue" class="code-generate-type-tag">
            {{ formatCodeGenType(appInfo.codeGeneratorType) }}
          </a-tag>
          <a-dropdown>
            <template #overlay>
              <a-menu>
                <a-menu-item key="detail" @click="showAppDetail">
                  <InfoCircleOutlined />
                  <span>应用详情</span>
                </a-menu-item>
                <a-menu-item v-if="isOwner || isAdmin" key="edit" @click="editApp">
                  <EditOutlined />
                  <span>编辑应用</span>
                </a-menu-item>
                <a-menu-divider v-if="isOwner || isAdmin" />
                <a-menu-item v-if="isOwner || isAdmin" key="delete" @click="deleteApp" danger>
                  <DeleteOutlined />
                  <span>删除应用</span>
                </a-menu-item>
              </a-menu>
            </template>
            <i class="ri-arrow-down-s-line"></i>
          </a-dropdown>
        </div>
      </div>

      <div class="header-center">
        <div class="tab-buttons">
          <a-button class="tab-button" :type="activeTab === 'display' ? 'primary' : 'default'"
            @click="activeTab = 'display'">
            <i class="ri-window-line"></i>
          </a-button>
          <a-button class="tab-button" :type="activeTab === 'code' ? 'primary' : 'default'" @click="activeTab = 'code'">
            <i class="ri-code-s-slash-line"></i>
          </a-button>
          <a-button class="tab-button" :type="activeTab === 'settings' ? 'primary' : 'default'"
            @click="activeTab = 'settings'">
            <i class="ri-settings-5-line"></i>
          </a-button>
        </div>

        <div class="action-buttons" :class="{ 'hidden-placeholder': activeTab !== 'display' }">
          <a-button class="tab-button" v-for="btn in actionButtons" :key="btn.key" @click="btn.handler"
            :loading="btn.loading">
            <a-tooltip placement="bottom">
              <template #title>
                {{ btn.label }}
              </template>
              <i :class="btn.icon"></i>
            </a-tooltip>
          </a-button>
          <a-button v-if="isOwner && previewUrl" type="link" :danger="isEditMode" @click="toggleEditMode"
            :class="{ 'edit-mode-active': isEditMode }" style="padding: 0; height: auto; margin-right: 12px">
            <template #icon>
              <EditOutlined />
            </template>
            {{ isEditMode ? '退出编辑' : '编辑模式' }}
          </a-button>
        </div>
      </div>

      <div class="header-right">
        <a-button @click="downloadApp" :loading="downloading" :disabled="!isOwner">
          <template #icon>
            <DownloadOutlined />
          </template>
        </a-button>
        <a-button class="deploy-button" @click="exportToMarkdown" :loading="exporting">
          <i class="ri-export-line"></i>
          导出记录
        </a-button>
        <a-button class="deploy-button" @click="deployApp" :loading="deploying">
          <i class="ri-rocket-line"></i>
          {{
            appInfo?.deployStatus == 0 ? '部署' : appInfo?.deployStatus == 1 ? '下线' : '部署失败'
          }}
        </a-button>
      </div>
    </div>

    <div class="main-content">
      <div class="chat-section">
        <transition name="fade">
          <div v-if="showScrollBottomBtn" class="scroll-bottom-btn" @click="scrollToBottomSmooth">
            <VerticalAlignBottomOutlined />
            <span class="new-msg-tip" v-if="isGenerating">新消息</span>
          </div>
        </transition>

        <div class="messages-container" ref="messagesContainer" @scroll="handleScroll">
          <div v-if="hasMoreHistory" class="load-more-container">
            <a-button type="link" @click="loadMoreHistory" :loading="loadingHistory" size="small">
              加载更多历史消息
            </a-button>
          </div>
          <div v-for="(message, index) in messages" :key="index" class="message-item">
            <div v-if="message.type === 'user'" class="user-message">
              <div class="message-content">{{ message.content }}</div>
              <div class="message-avatar">
                <a-avatar :src="userStore.user.avatar" />
              </div>
            </div>
            <div v-else class="ai-message">
              <div class="message-avatar">
                <a-avatar :src="aiAvatar" />
              </div>
              <div class="message-content">
                <div v-if="message.workflowSteps && message.workflowSteps.length > 0" class="workflow-box">
                  <div class="workflow-header" @click="message.isThinkingExpanded = !message.isThinkingExpanded">
                    <div class="header-left">
                      <a-spin v-if="message.loading" size="small" style="margin-right: 6px" />
                      <i v-else class="ri-checkbox-circle-fill" style="color: #52c41a; margin-right: 6px"></i>

                      <span class="status-text">{{ message.workflowStatus || '思考过程' }}</span>
                      <span class="step-count" v-if="!message.loading">
                        (共 {{ message.workflowSteps.length }} 步)</span>
                    </div>
                    <i class="ri-arrow-down-s-line arrow-icon" :class="{ expanded: message.isThinkingExpanded }"></i>
                  </div>

                  <div v-if="message.isThinkingExpanded" class="workflow-body">
                    <div v-for="(step, idx) in message.workflowSteps" :key="idx" class="step-item">
                      <div class="step-indicator">
                        <div class="step-line" v-if="idx !== message.workflowSteps.length - 1"></div>
                        <div class="step-dot" :class="step.type"></div>
                      </div>

                      <div class="step-content">
                        <div class="step-title">{{ step.content }}</div>
                        <div v-if="step.type === 'reasoning' && step.extendedContent" class="reasoning-text">
                          {{ step.extendedContent }}
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <MarkdownRenderer v-if="message.content" :content="message.content" />

                <div v-if="message.loading || message.workflowStatus" class="loading-indicator">
                  <a-spin v-if="message.loading" size="small" />
                  <span>{{ message.workflowStatus || '正在思考...' }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <a-alert v-if="selectedElementInfo" class="selected-element-alert" type="info" closable
          @close="clearSelectedElement">
          <template #message>
            <div class="selected-element-info">
              <div class="element-header">
                <span class="element-tag">
                  选中元素：{{ selectedElementInfo.tagName.toLowerCase() }}
                </span>
                <span v-if="selectedElementInfo.id" class="element-id">
                  #{{ selectedElementInfo.id }}
                </span>
                <span v-if="selectedElementInfo.className" class="element-class">
                  .{{ selectedElementInfo.className.split(' ').join('.') }}
                </span>
              </div>
              <div class="element-details">
                <div v-if="selectedElementInfo.textContent" class="element-item">
                  内容: {{ selectedElementInfo.textContent.substring(0, 50) }}
                  {{ selectedElementInfo.textContent.length > 50 ? '...' : '' }}
                </div>
                <div v-if="selectedElementInfo.pagePath" class="element-item">
                  页面路径: {{ selectedElementInfo.pagePath }}
                </div>
                <div class="element-item">
                  选择器:
                  <code class="element-selector-code">{{ selectedElementInfo.selector }}</code>
                </div>
              </div>
            </div>
          </template>
        </a-alert>

        <div class="input-container">
          <div class="input-wrapper">
            <a-tooltip v-if="!isOwner" title="无法在别人的作品下对话哦~" placement="top">
              <a-textarea class="chat-content" v-model:value="userInput" placeholder="请描述你想生成的网站，越详细效果越好哦" :rows="4"
                :maxlength="1000" @keydown.enter.prevent="sendMessage" :disabled="isGenerating || !isOwner" />
            </a-tooltip>
            <a-textarea class="chat-content" v-else v-model:value="userInput" placeholder="请描述你想生成的网站，越详细效果越好哦"
              :rows="4" :maxlength="1000" @keydown.enter.prevent="sendMessage" :disabled="isGenerating" />
            <div class="input-actions">
              <a-button class="sent-message-btn" type="primary" @click="sendMessage" :loading="isGenerating"
                :disabled="!isOwner">
                <template #icon>
                  <SendOutlined />
                </template>
              </a-button>
            </div>
          </div>
        </div>
      </div>

      <div class="content-section">
        <div v-if="activeTab === 'display'" class="tab-content display-content">
          <div v-if="!previewUrl && !isCodeGenerating" class="preview-placeholder">
            <div class="placeholder-icon">🌐</div>
            <p>网站文件生成完成后将在这里展示</p>
          </div>
          <div v-else-if="isCodeGenerating" class="preview-loading">
            <a-spin size="large" />
            <p>正在生成网站...</p>
          </div>
          <iframe v-else :src="previewUrl" class="preview-iframe" frameborder="0" @load="onIframeLoad"></iframe>
        </div>

        <div v-else-if="activeTab === 'code'" class="tab-content code-content">
          <div v-if="files.length === 0" class="code-placeholder">
            <div class="placeholder-icon">📝</div>
            <p>代码文件生成后将在这里显示</p>
          </div>
          <div v-else class="editor-container">
            <div class="tabs">
              <div v-for="f in files" :key="f.id" class="tab"
                :class="{ active: f.id === activeFileId, generating: f.isGenerating }" @click="switchFile(f.id)">
                <span>{{ f.name }}</span>
                <a-spin v-if="f.isGenerating" size="small" style="margin-left: 8px" />
              </div>
            </div>
            <CodeEditor v-model:value="code" :language="language" :options="editorOptions" class="editor"
              @change="onCodeChange" />
          </div>
        </div>

        <div v-else-if="activeTab === 'settings'" class="tab-content settings-content">
          <div class="settings-placeholder">
            <div class="placeholder-icon">⚙️</div>
            <p>设置区域（预留后端数据注入）</p>
          </div>
        </div>
      </div>
    </div>

    <AppDetailModal v-model:open="appDetailVisible" :app="appInfo" :show-actions="isOwner || isAdmin" @edit="editApp"
      @delete="deleteApp" />

    <DeploySuccessModal v-model:open="deployModalVisible" :deploy-url="deployUrl" @open-site="openDeployedSite" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import * as monaco from 'monaco-editor'
import { CodeEditor } from 'monaco-editor-vue3'
import { useUserStore } from '@/stores/user'
import {
  getAppVoById,
  deploy as deployAppApi,
  deleteApp as deleteAppApi,
  offline
} from '@/api/appController'
import { CodeGenTypeEnum, formatCodeGenType } from '@/utils/codeGenerateTypes'
import request from '@/request'

import MarkdownRenderer from '@/components/MarkdownRenderer.vue'
import AppDetailModal from '@/components/AppDetailModal.vue'
import DeploySuccessModal from '@/components/DeploySuccessModal.vue'
import aiAvatar from '@/assets/aiAvatar.png'
import { API_BASE_URL, getStaticPreviewUrl } from '@/config/env'

import {
  SendOutlined,
  InfoCircleOutlined,
  EditOutlined,
  DeleteOutlined,
  DownloadOutlined,
  VerticalAlignBottomOutlined, // 已添加引入
} from '@ant-design/icons-vue'
import { exportMarkdown, listAppChatHistory } from '@/api/chatHistoryController'

import { VisualEditor, type ElementInfo } from '@/utils/visualEditor'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 应用信息
const appInfo = ref<API.AppVO>()
const appId = ref<string>()

// 标签切换状态
const activeTab = ref<'display' | 'code' | 'settings'>('display')

// 工作流阶段步骤
interface WorkflowStep {
  step: number
  content: string
  type: string
  extendedContent?: string
}

// 代码文件接口
interface CodeFile {
  id: string
  name: string
  value: string
  language: string
  isGenerating?: boolean // 是否正在生成中
}

// 对话相关
interface Message {
  type: 'user' | 'ai'
  content: string
  loading?: boolean
  workflowStatus?: string
  // 步骤列表
  workflowSteps?: WorkflowStep[]
  // 是否展开详情
  isThinkingExpanded?: boolean
}

const messages = ref<Message[]>([])
const userInput = ref('')
const isGenerating = ref(false)
const isCodeGenerating = ref(false) // 是否正在生成代码
const messagesContainer = ref<HTMLElement>()
const hasInitialConversation = ref(false) // 标记是否已经进行过初始对话

const code = ref('')
const language = ref('javascript')
const files = ref<CodeFile[]>([])
const activeFileId = ref('')
const models = new Map()
let fileIdCounter = 0 // 用于生成唯一的文件 ID

const editorOptions = {
  readOnly: false,
  minimap: { enabled: true },
  fontSize: 14,
  wordWrap: 'on',
  automaticLayout: true,
}

// 对话历史相关
const loadingHistory = ref(false)
const hasMoreHistory = ref(false)
const lastCreateTime = ref<string>()
const historyLoaded = ref(false)

// 预览相关
const previewUrl = ref('')
const previewReady = ref(false)

// 可视化编辑相关
const isEditMode = ref(false)
const selectedElementInfo = ref<ElementInfo | null>(null)
const visualEditor = new VisualEditor({
  onElementSelected: (elementInfo: ElementInfo) => {
    selectedElementInfo.value = elementInfo
  },
})

// 部署相关
const deploying = ref(false)
const deployModalVisible = ref(false)
const deployUrl = ref('')

// 导出相关
const exporting = ref(false)

// 权限相关
const isOwner = computed(() => {
  return appInfo.value?.userId === userStore.user.id
})
const isAdmin = computed(() => {
  return userStore.user.role === 'admin'
})

// 应用详情相关
const appDetailVisible = ref(false)

// 头部功能按钮配置（仅在"显示"标签时显示，方便后期修改）
const actionButtons = ref([
  {
    key: 'showAll',
    label: '全部显示',
    icon: 'ri-fullscreen-line',
    handler: () => showAllContent(),
    loading: false,
  },
  {
    key: 'refresh',
    label: '刷新',
    icon: 'ri-refresh-line',
    handler: () => refreshPreview(),
    loading: false,
  },
  {
    key: 'openNew',
    label: '新窗口打开',
    icon: 'ri-send-plane-line',
    handler: () => openInNewTab(),
    loading: false,
  },
])

// 显示应用详情
const showAppDetail = () => {
  appDetailVisible.value = true
}

// 加载对话历史
const loadChatHistory = async (isLoadMore = false) => {
  if (!appId.value || loadingHistory.value) return
  loadingHistory.value = true
  try {
    const params: API.listAppChatHistoryParams = {
      appId: appId.value as unknown as number,
      pageNum: 10,
    }
    // 如果是加载更多，传递最老一条消息的创建时间作为游标
    if (isLoadMore && lastCreateTime.value) {
      params.lastCreateTime = lastCreateTime.value
    }
    const res = await listAppChatHistory(params)
    if (res.data.code === 0 && res.data.data) {
      const chatHistories = res.data.data.records || []
      if (chatHistories.length > 0) {
        // 需要反转数组，让老消息在前 (后端通常返回按时间降序)
        const historyMessages: Message[] = chatHistories.reverse().map((chat) => {
          // === 1. 解析思考过程 (Thinking Content) ===
          let restoredSteps: WorkflowStep[] = []
          let statusText = undefined

          if (chat.thinkingContent) {
            try {
              // 尝试解析 JSON 字符串
              const steps = JSON.parse(chat.thinkingContent)
              if (Array.isArray(steps)) {
                // 映射为前端 WorkflowStep 结构
                restoredSteps = steps.map((s: any) => ({
                  step: s.step,
                  content: s.content,
                  type: s.type,
                  extendedContent: s.extendedContent, // 包含 DeepSeek 的详细思考
                }))
                // 如果存在步骤，状态标记为完成
                statusText = '生成完成'
              }
            } catch (e) {
              console.error('解析历史思考过程失败', e)
            }
          }

          const typeStr = String(chat.messageType)
          const isUser = typeStr === 'user' || typeStr === '1'
          // === 2. 构造消息对象 ===
          return {
            type: isUser ? 'user' : 'ai',
            content: chat.message || '',
            loading: false,
            workflowSteps: restoredSteps,
            workflowStatus: statusText,
            isThinkingExpanded: false,
          }
        })

        if (isLoadMore) {
          // 加载更多时，将历史消息添加到开头
          messages.value.unshift(...historyMessages)
        } else {
          // 初始加载，直接设置消息列表
          messages.value = historyMessages
          await nextTick()
          if (messagesContainer.value) {
            // 1. 临时强制关闭平滑滚动 (设为 auto)
            messagesContainer.value.style.scrollBehavior = 'auto'
            // 2. 执行滚动 (瞬间完成)
            messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
            // 3. 恢复平滑滚动
            setTimeout(() => {
              if (messagesContainer.value) {
                messagesContainer.value.style.scrollBehavior = ''
              }
            }, 0)
          }
        }
        lastCreateTime.value = chatHistories[0]?.createTime
        // 检查是否还有更多历史
        hasMoreHistory.value = chatHistories.length === 10
      } else {
        hasMoreHistory.value = false
      }
      historyLoaded.value = true
    }
  } catch (error) {
    console.error('加载对话历史失败：', error)
    message.error('加载对话历史失败')
  } finally {
    loadingHistory.value = false
  }
}
// 加载更多历史消息
const loadMoreHistory = async () => {
  await loadChatHistory(true)
}

// 获取应用信息
const fetchAppInfo = async () => {
  const id = route.params.id as string
  if (!id) {
    message.error('应用ID不存在')
    router.push('/')
    return
  }

  appId.value = id

  try {
    const res = await getAppVoById({ id: id as unknown as number })
    if (res.data.code === 0 && res.data.data) {
      appInfo.value = res.data.data
    } else {
      message.error('获取应用信息失败')
      router.push('/')
    }
  } catch (error) {
    console.error('获取应用信息失败：', error)
    message.error('获取应用信息失败')
    router.push('/')
  }
}

// 发送初始消息
const sendInitialMessage = async (prompt: string) => {
  // 添加用户消息
  messages.value.push({
    type: 'user',
    content: prompt,
  })

  // 添加AI消息占位符
  const aiMessageIndex = messages.value.length
  messages.value.push({
    type: 'ai',
    content: '',
    loading: true,
  })

  await nextTick()
  scrollToBottom()

  // 开始生成
  isGenerating.value = true
  await generateCode(prompt, aiMessageIndex)
}

// 发送消息
const sendMessage = async () => {
  if (!userInput.value.trim() || isGenerating.value) {
    return
  }

  let message = userInput.value.trim()
  // 如果有选中的元素，将元素信息添加到提示词中
  if (selectedElementInfo.value) {
    let elementContext = `\n\n选中元素信息：`
    if (selectedElementInfo.value.pagePath) {
      elementContext += `\n- 页面路径: ${selectedElementInfo.value.pagePath}`
    }
    elementContext += `\n- 标签: ${selectedElementInfo.value.tagName.toLowerCase()}\n- 选择器: ${selectedElementInfo.value.selector}`
    if (selectedElementInfo.value.textContent) {
      elementContext += `\n- 当前内容: ${selectedElementInfo.value.textContent.substring(0, 100)}`
    }
    message += elementContext
  }
  userInput.value = ''

  // 添加用户消息
  messages.value.push({
    type: 'user',
    content: message,
  })

  // 发送消息后，清除选中元素并退出编辑模式
  if (selectedElementInfo.value) {
    clearSelectedElement()
    if (isEditMode.value) {
      toggleEditMode()
    }
  }

  // 添加AI消息占位符
  const aiMessageIndex = messages.value.length
  messages.value.push({
    type: 'ai',
    content: '',
    loading: true,
  })

  await nextTick()
  scrollToBottom()

  // 开始生成
  isGenerating.value = true
  await generateCode(message, aiMessageIndex)
}

// 生成代码 - 使用 EventSource 处理流式响应
const generateCode = async (userMessage: string, aiMessageIndex: number) => {
  let eventSource: EventSource | null = null
  let streamCompleted = false
  let hadCodeGeneration = false

  try {
    const baseURL = request.defaults.baseURL || API_BASE_URL
    const params = new URLSearchParams({
      appId: appId.value || '',
      userMessage: userMessage,
    })
    const url = `${baseURL}/app/chat/generate/code?${params}`

    // 创建 EventSource 连接
    eventSource = new EventSource(url, {
      withCredentials: true,
    })

    let fullContent = ''

    // 处理接收到的消息
    eventSource.onmessage = function (event) {
      if (streamCompleted) return

      try {
        const parsed = JSON.parse(event.data)
        const rawChunk = parsed.d

        if (rawChunk !== undefined && rawChunk !== null) {
          const aiMessage = messages.value[aiMessageIndex]
          if (!aiMessage) return

          try {
            const trimmedChunk = rawChunk.trim()
            if (trimmedChunk.startsWith('{') && trimmedChunk.endsWith('}')) {
              const chunkObj = JSON.parse(trimmedChunk)
            }
          } catch (e) {
            // 解析失败，继续处理
          }

          // 初始化数组
          if (!aiMessage.workflowSteps) {
            aiMessage.workflowSteps = []
            aiMessage.isThinkingExpanded = false
          }

          let isWorkflowEvent = false
          let isToolCallEvent = false

          try {
            const trimmedChunk = rawChunk.trim()
            // 简单的预检查，避免对每一行代码都进行 JSON.parse
            if (trimmedChunk.startsWith('{') && trimmedChunk.endsWith('}')) {
              const chunkObj = JSON.parse(trimmedChunk)

              // === 1. 检查工作流事件 ===
              if (
                chunkObj &&
                chunkObj.type &&
                ['start', 'processing', 'finish', 'error'].includes(chunkObj.type)
              ) {
                isWorkflowEvent = true

                const lastStep = aiMessage.workflowSteps[aiMessage.workflowSteps.length - 1]
                if (!lastStep || lastStep.content !== chunkObj.content) {
                  aiMessage.workflowSteps.push({
                    step: chunkObj.step,
                    content: chunkObj.content,
                    type: chunkObj.type,
                  })
                }

                // 是工作流状态消息
                if (chunkObj.type === 'finish') {
                  aiMessage.workflowStatus = '生成完成'
                } else if (chunkObj.type === 'error') {
                  aiMessage.workflowStatus = '执行出错'
                } else {
                  aiMessage.workflowStatus = chunkObj.content
                  aiMessage.loading = true
                }
              }
              // 检查 tool_call 事件（文件操作）
              else if (chunkObj.type === 'tool_request' || chunkObj.type === 'tool_executed') {
                isToolCallEvent = true

                if (chunkObj.type === 'tool_request') {
                  hadCodeGeneration = true
                  isCodeGenerating.value = true
                  console.log('🔧 检测到工具调用，标记为代码生成')
                }

                handleToolCallEvent(chunkObj)
              }
            }
          } catch (e) { }

          // 是普通代码/文本片段
          if (!isWorkflowEvent && !isToolCallEvent) {
            const cleanChunk = rawChunk.replace(/<think>[\s\S]*?<\/think>/g, '')
            fullContent += cleanChunk
            aiMessage.content = fullContent
            // 收到代码内容时，保持 loading 为 true，直到流结束
          }
          scrollToBottom()
        }
      } catch (error) {
        console.error('解析消息失败:', error)
        handleError(error, aiMessageIndex)
      }
    }

    // 处理 done 事件 (流结束)
    eventSource.addEventListener('done', function () {
      if (streamCompleted) return

      streamCompleted = true

      // 确保最后状态正确
      const aiMessage = messages.value[aiMessageIndex]
      if (aiMessage) {
        aiMessage.loading = false
      }

      isGenerating.value = false
      isCodeGenerating.value = false

      eventSource?.close()

      // 延迟刷新预览，确保后端构建文件已完全写入
      if (hadCodeGeneration) {
        console.log('代码生成完成，刷新预览')
        setTimeout(async () => {
          await fetchAppInfo()
          updatePreview()
        }, 1000)
        hadCodeGeneration = false
      } else {
        console.log('普通聊天完成，不刷新预览')
      }
    })

    // 处理business-error事件（后端限流等错误）
    eventSource.addEventListener('business-error', function (event: MessageEvent) {
      if (streamCompleted) return

      try {
        const errorData = JSON.parse(event.data)
        console.error('SSE业务错误事件:', errorData)

        // 显示具体的错误信息
        const errorMessage = errorData.message || '生成过程中出现错误'

        const aiMessage = messages.value[aiMessageIndex]
        if (aiMessage) {
          aiMessage.content = `❌ ${errorMessage}`
          aiMessage.loading = false
        }

        message.error(errorMessage)

        streamCompleted = true
        isGenerating.value = false
        eventSource?.close()
      } catch (parseError) {
        console.error('解析错误事件失败:', parseError, '原始数据:', event.data)
        handleError(new Error('服务器返回错误'), aiMessageIndex)
      }
    })

    // 处理错误
    eventSource.onerror = function () {
      if (streamCompleted || !isGenerating.value) return

      // 检查是否是正常的连接关闭 (某些浏览器/服务端实现可能会触发这个)
      if (eventSource?.readyState === EventSource.CONNECTING) {
        // 视为连接中断或结束
        streamCompleted = true
        isGenerating.value = false

        const aiMessage = messages.value[aiMessageIndex]
        if (aiMessage) aiMessage.loading = false

        eventSource?.close()

        setTimeout(async () => {
          await fetchAppInfo()
          updatePreview()
        }, 1000)
      } else {
        // 真正的错误
        handleError(new Error('SSE连接错误'), aiMessageIndex)
        eventSource?.close()
      }
    }
  } catch (error) {
    console.error('创建 EventSource 失败：', error)
    handleError(error, aiMessageIndex)
  }
}

// 错误处理函数
const handleError = (error: unknown, aiMessageIndex: number) => {
  console.error('生成代码失败：', error)
  const aiMessage = messages.value[aiMessageIndex]
  if (aiMessage) {
    aiMessage.content = '抱歉，生成过程中出现了错误，请重试。'
    aiMessage.loading = false
  }
  message.error('生成失败，请重试')
  // 重置所有状态
  isGenerating.value = false
  isCodeGenerating.value = false
}

// 更新预览
const updatePreview = () => {
  if (appId.value) {
    const codeGenType = appInfo.value?.codeGeneratorType || CodeGenTypeEnum.HTML
    const newPreviewUrl = getStaticPreviewUrl(codeGenType, appId.value)
    previewUrl.value = newPreviewUrl
    previewReady.value = true
  }
}

// 滚动到底部 (旧版，保留用于普通聊天)
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// === 回到底部按钮逻辑 ===
const showScrollBottomBtn = ref(false)

// 监听滚动事件
const handleScroll = () => {
  const el = messagesContainer.value
  if (!el) return
  const distanceToBottom = el.scrollHeight - el.scrollTop - el.clientHeight
  showScrollBottomBtn.value = distanceToBottom > 150
}

// 平滑滚动到底部（点击按钮时调用）
const scrollToBottomSmooth = () => {
  const el = messagesContainer.value
  if (el) {
    el.scrollTo({
      top: el.scrollHeight,
      behavior: 'smooth',
    })
  }
}

// === 处理 Tool Call 事件（文件操作）===
const handleToolCallEvent = (toolEvent: any) => {
  try {
    // console.log('handleToolCallEvent 的', toolEvent)

    // tool_request: AI 请求执行工具
    if (toolEvent.type === 'tool_request') {
      const toolName = toolEvent.tool_name
      const params = toolEvent.parameters || {}

      // 检测文件写入操作
      if (toolName === 'writeFile' || toolName === 'Write' || toolName === 'StrReplace') {
        const filePath = params.relativePath || params.relativeFilePath || params.path
        if (filePath) {
          const fileName = filePath.split('/').pop() || filePath.split('\\').pop() || filePath

          // 查找或创建文件
          let file = files.value.find(f => f.name === fileName)
          if (!file) {
            console.log('✨ 创建新文件:', fileName)
            file = {
              id: `file-${++fileIdCounter}`,
              name: fileName,
              value: '',
              language: getLanguageByExtension(fileName),
              isGenerating: true
            }
            files.value.push(file)

            // 自动切换到新文件并切换到 Code 标签页
            activeFileId.value = file.id
            activeTab.value = 'code'

            // 创建 Monaco 模型
            const model = monaco.editor.createModel('', file.language)
            models.set(file.id, model)
            code.value = ''
            language.value = file.language
          } else {
            file.isGenerating = true
          }
        }
      }
    }
    // tool_executed: 工具执行完成
    else if (toolEvent.type === 'tool_executed') {
      const toolName = toolEvent.tool_name
      const params = toolEvent.parameters || {}
      const result = toolEvent.result

      // 文件写入完成
      if (toolName === 'writeFile' || toolName === 'Write') {
        // ⭐ 使用正确的字段名：relativePath 和 content
        const filePath = params.relativePath || params.relativeFilePath || params.path
        const contents = params.content || params.contents || ''

        console.log('📄 文件信息:', { filePath, contentLength: contents.length })

        if (filePath) {
          const fileName = filePath.split('/').pop() || filePath.split('\\').pop() || filePath
          const file = files.value.find(f => f.name === fileName)

          if (file) {
            console.log('📝 使用打字机效果显示代码:', fileName)
            // 使用打字机效果逐字显示代码
            typewriterEffect(file.id, contents)
          } else {
            console.warn('⚠️ 未找到文件:', fileName)
          }
        }
      }
      // 文件替换完成
      else if (toolName === 'StrReplace') {
        const filePath = params.relativePath || params.relativeFilePath || params.path
        const newString = params.new_string || ''

        if (filePath) {
          const fileName = filePath.split('/').pop() || filePath.split('\\').pop() || filePath
          const file = files.value.find(f => f.name === fileName)

          if (file) {
            // 简单处理：追加新内容（实际应该做字符串替换，但这里简化处理）
            const currentContent = file.value
            const updatedContent = currentContent + newString
            typewriterEffect(file.id, updatedContent)
          }
        }
      }
    }
  } catch (error) {
    console.error('❌ 处理 Tool Call 事件失败:', error)
  }
}

// === 打字机效果显示代码 ===
const typewriterEffect = (fileId: string, fullContent: string) => {
  const file = files.value.find(f => f.id === fileId)
  if (!file) return

  const model = models.get(fileId)
  if (!model) return

  // 每次添加的字符数（可以调整速度）
  const chunkSize = 5
  let currentIndex = 0

  const typeNextChunk = () => {
    if (currentIndex < fullContent.length) {
      currentIndex += chunkSize
      const partialContent = fullContent.substring(0, currentIndex)

      // 更新文件内容
      file.value = partialContent
      model.setValue(partialContent)

      // 如果当前正在查看这个文件，更新编辑器
      if (activeFileId.value === fileId) {
        code.value = partialContent
      }

      // 继续下一帧
      requestAnimationFrame(typeNextChunk)
    } else {
      // 完成
      file.value = fullContent
      file.isGenerating = false
      model.setValue(fullContent)

      if (activeFileId.value === fileId) {
        code.value = fullContent
      }
    }
  }

  typeNextChunk()
}

// 切换文件
const switchFile = (id: string) => {
  activeFileId.value = id
  const file = files.value.find((f) => f.id === id)
  if (file) {
    if (!models.has(id)) {
      const model = monaco.editor.createModel(file.value, getLanguageByExtension(file.name))
      models.set(id, model)
    }
  }

  const model = models.get(id)

  // 设置编辑器内容（v-model）
  code.value = model.getValue()
  language.value = model.getLanguageId()
}

// 当切换文件,需要更改代码编辑器的内容
const onCodeChange = (newVal: any) => {
  const model = models.get(activeFileId.value)
  if (model && newVal !== model.getValue()) {
    model.setValue(newVal)
  }
}

// 获取文件扩展名,用于高亮显示
const getLanguageByExtension = (name: string) => {
  const ext = name.split('.').pop()
  switch (ext) {
    case 'js':
      return 'javascript'
    case 'ts':
      return 'typescript'
    case 'vue':
      return 'vue'
    case 'json':
      return 'json'
    case 'html':
      return 'html'
    case 'css':
      return 'css'
    case 'scss':
      return 'scss'
    case 'md':
      return 'markdown'
    default:
      return 'plaintext'
  }
}

// 部署应用
const deployApp = async () => {
  if (!appId.value) {
    message.error('应用ID不存在')
    return
  }

  deploying.value = true
  try {
    switch (appInfo.value?.deployStatus) {
      case 0:
      case 2:
        const res1 = await deployAppApi({
          appId: appId.value as unknown as number,
        })

        if (res1.data.code === 0 && res1.data.data) {
          deployUrl.value = res1.data.data
          deployModalVisible.value = true
          appInfo.value.deployStatus = 1
          message.success('部署成功')
        } else {
          message.error('部署失败：' + res1.data.message)
        }
        break
      case 1:
        const res2 = await offline({
          appId: appId.value as unknown as number,
        })
        if (res2.data.code === 0 && res2.data.data) {
          appInfo.value.deployStatus = 0
          message.info('应用部署下线成功')
        } else {
          message.error('下线失败：' + res2.data.message)
        }

        break
    }
  } catch (error) {
    console.error('部署失败：', error)
    message.error('部署失败，请重试')
  } finally {
    deploying.value = false
  }
}

// 导出 Markdown 聊天记录
const exportToMarkdown = async () => {
  if (!appId.value) {
    message.error('应用ID不存在')
    return
  }

  exporting.value = true
  try {
    const res = await exportMarkdown({
      appId: appId.value as unknown as number,
    })

    if (res.data.code === 0 && res.data.data) {
      const markdown = res.data.data
      const blob = new Blob([markdown], { type: 'text/markdown;charset=utf-8' })
      const url = URL.createObjectURL(blob)

      const link = document.createElement('a')
      link.href = url
      link.download = `chat-history-${appId.value}.md`
      link.click()

      URL.revokeObjectURL(url)
      message.success('导出成功')
    } else {
      message.error('导出失败：' + res.data.message)
    }
  } catch (error) {
    console.error('导出失败：', error)
    message.error('导出失败，请重试')
  } finally {
    exporting.value = false
  }
}

// 在新窗口打开预览
const openInNewTab = () => {
  if (previewUrl.value) {
    window.open(previewUrl.value, '_blank')
  }
}

// 打开部署的网站
const openDeployedSite = () => {
  if (deployUrl.value) {
    window.open(deployUrl.value, '_blank')
  }
}

// iframe加载完成
const onIframeLoad = () => {
  previewReady.value = true
  const iframe = document.querySelector('.preview-iframe') as HTMLIFrameElement
  if (iframe) {
    visualEditor.init(iframe)
    visualEditor.onIframeLoad()
  }
}

// 编辑应用
const editApp = () => {
  if (appInfo.value?.id) {
    router.push(`/app/edit/${appInfo.value.id}`)
  }
}

// 删除应用
const deleteApp = async () => {
  if (!appInfo.value?.id) return

  try {
    const res = await deleteAppApi({ id: appInfo.value.id })
    if (res.data.code === 0) {
      message.success('删除成功')
      appDetailVisible.value = false
      router.push('/')
    } else {
      message.error('删除失败：' + res.data.message)
    }
  } catch (error) {
    console.error('删除失败：', error)
    message.error('删除失败')
  }
}

// 全部显示功能
const showAllContent = () => {
  message.info('全部显示功能（预留）')
}

// 刷新预览
const refreshPreview = () => {
  if (previewUrl.value) {
    updatePreview()
    message.success('刷新成功')
  } else {
    message.warning('暂无可刷新的内容')
  }
}

// 下载相关
const downloading = ref(false)

// 下载应用
const downloadApp = async () => {
  if (!appId.value) {
    message.error('应用ID不存在')
    return
  }
  downloading.value = true
  try {
    const API_BASE_URL = request.defaults.baseURL || ''
    const url = `${API_BASE_URL}/app/download/${appId.value}`
    const response = await fetch(url, {
      method: 'GET',
      credentials: 'include',
    })
    if (!response.ok) {
      throw new Error(`下载失败: ${response.status}`)
    }
    // 获取文件名
    const contentDisposition = response.headers.get('Content-Disposition')
    const fileName = contentDisposition?.match(/filename="(.+)"/)?.[1] || `app-${appId.value}.zip`
    // 下载文件
    const blob = await response.blob()
    const downloadUrl = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = fileName
    link.click()
    // 清理
    URL.revokeObjectURL(downloadUrl)
    message.success('代码下载成功')
  } catch (error) {
    console.error('下载失败：', error)
    message.error('下载失败，请重试')
  } finally {
    downloading.value = false
  }
}

// 初始化页面
const initPage = async () => {
  // 1. 先获取应用信息
  await fetchAppInfo()

  if (!appId.value || !appInfo.value) {
    return
  }

  // 2. 加载对话历史
  await loadChatHistory()

  // 3. 如果有至少 2 条对话记录，展示对应的网站
  if (messages.value.length >= 2) {
    updatePreview()
  }

  // 4. 如果是自己的 app，并且没有对话历史，才自动将 initPrompt 作为第一条消息触发对话
  if (
    isOwner.value &&
    messages.value.length === 0 &&
    appInfo.value.initPrompt &&
    !hasInitialConversation.value
  ) {
    hasInitialConversation.value = true
    await sendInitialMessage(appInfo.value.initPrompt)
  }
}

// 可视化编辑相关函数
const toggleEditMode = () => {
  // 检查 iframe 是否已经加载
  const iframe = document.querySelector('.preview-iframe') as HTMLIFrameElement
  if (!iframe) {
    message.warning('请等待页面加载完成')
    return
  }
  // 确保 visualEditor 已初始化
  if (!previewReady.value) {
    message.warning('请等待页面加载完成')
    return
  }
  const newEditMode = visualEditor.toggleEditMode()
  isEditMode.value = newEditMode
}

const clearSelectedElement = () => {
  selectedElementInfo.value = null
  visualEditor.clearSelection()
}

const getInputPlaceholder = () => {
  if (selectedElementInfo.value) {
    return `正在编辑 ${selectedElementInfo.value.tagName.toLowerCase()} 元素，描述您想要的修改...`
  }
  return '请描述你想生成的网站，越详细效果越好哦'
}

// 页面加载时初始化
const handleIframeMessage = (event: MessageEvent) => {
  visualEditor.handleIframeMessage(event)
}

onMounted(() => {
  initPage()
  window.addEventListener('message', handleIframeMessage)
})

// 清理资源
onUnmounted(() => {
  window.removeEventListener('message', handleIframeMessage)
  // EventSource 会在组件卸载时自动清理
})
</script>

<style scoped>
#appChatPage {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
  overflow: hidden;
}

/* 自定义头部区域 */
.custom-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  background: #ffffff;
  z-index: 100;
}

/* 头部左侧 */
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.project-name-container {
  display: flex;
  align-items: center;
  gap: 4px;
}

.project-name {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.code-generate-type-tag {
  font-size: 12px;
}

/* 头部中间 */
.header-center {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  justify-content: center;
}

.tab-buttons {
  display: flex;
  background-color: #f5f5f5;
  border-radius: 10px;
}

.tab-button {
  padding: 4px 8px !important;
  margin: 8px !important;
}

.tab-button:nth-child(2) {
  margin: 8px 0px !important;
}

.action-buttons {
  display: flex;
  padding-left: 16px;
  border-left: 1px solid #e8e8e8;
}

:where(.css-dev-only-do-not-override-1p3hq3p).tab-button.ant-btn-default {
  background-color: #f5f5f5;
  border-color: transparent;
}

/* 隐藏但保持空间占位 */
.hidden-placeholder {
  visibility: hidden;
  pointer-events: none;
}

/* 头部右侧 */
.header-right {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
}

/* 部署按钮样式（黑色） */
.deploy-button {
  background-color: #1a1a1a;
  border-color: #1a1a1a;
  color: #ffffff;
}

.deploy-button:hover {
  background-color: #333333;
  border-color: #333333;
  color: #ffffff;
}

.deploy-button:active,
.deploy-button:focus {
  background-color: #000000;
  border-color: #000000;
  color: #ffffff;
}

/* 主要内容区域 */
.main-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 左侧对话区域 */
.chat-section {
  position: relative;
  flex: 2;
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

/* === 回到底部按钮 === */
.scroll-bottom-btn {
  position: absolute;
  bottom: 160px;
  /* 位于输入框上方 */
  right: 24px;
  width: 40px;
  height: 40px;
  background-color: #fff;
  border: 1px solid #e8e8e8;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  z-index: 10;
  color: #666;
  transition: all 0.3s;
}

.scroll-bottom-btn:hover {
  background-color: #1890ff;
  color: white;
  border-color: #1890ff;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(24, 144, 255, 0.2);
}

/* 新消息提示气泡 */
.new-msg-tip {
  position: absolute;
  top: -25px;
  background: #1890ff;
  color: white;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  white-space: nowrap;
}

.new-msg-tip::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 50%;
  transform: translateX(-50%);
  border-left: 4px solid transparent;
  border-right: 4px solid transparent;
  border-top: 4px solid #1890ff;
}

/* Vue 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition:
    opacity 0.3s ease,
    transform 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

/* === 消息容器优化 === */
.messages-container {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  scroll-behavior: smooth;
  background-color: #f8f9fa;
  /* 更柔和的灰背景 */
}

.message-item {
  margin-bottom: 24px;
}

.user-message {
  display: flex;
  justify-content: flex-end;
  align-items: flex-start;
  gap: 8px;
}

.ai-message {
  display: flex;
  justify-content: flex-start;
  align-items: flex-start;
  gap: 8px;
}

.message-content {
  max-width: 80%;
  padding: 12px 16px;
  word-wrap: break-word;
}

/* 用户气泡优化 */
.user-message .message-content {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  color: white;
  /* 非对称圆角 */
  border-radius: 16px 16px 4px 16px;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.2);
  font-size: 14px;
  line-height: 1.6;
}

/* AI 气泡优化 */
.ai-message .message-content {
  background: #ffffff;
  color: #333;
  /* 非对称圆角 */
  border-radius: 16px 16px 16px 4px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  padding: 16px 20px;
  font-size: 15px;
  line-height: 1.7;
}

/* 头像微调 */
.message-avatar {
  flex-shrink: 0;
}

.message-avatar :deep(.ant-avatar) {
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  border: 2px solid white;
  height: 48px;
  width: 48px;
}

.loading-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
}

/* === 思考过程容器优化 === */
.workflow-box {
  margin-bottom: 16px;
  border: none;
  border-radius: 8px;
  background-color: #f7f7f8;
  overflow: hidden;
  font-size: 13px;
  transition: all 0.3s;
}

.workflow-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  cursor: pointer;
  background: transparent;
  color: #666;
  user-select: none;
  border-bottom: 1px solid transparent;
  transition: background 0.2s;
}

.workflow-header:hover {
  background: rgba(0, 0, 0, 0.02);
  color: #333;
}

/* 展开时的分割线 */
.workflow-box:has(.workflow-body) .workflow-header {
  border-bottom: 1px solid #e5e5e5;
}

.header-left {
  display: flex;
  align-items: center;
}

.status-text {
  font-weight: 500;
}

.step-count {
  font-size: 12px;
  color: #9ca3af;
  margin-left: 4px;
}

.arrow-icon {
  transition: transform 0.2s;
  color: #6b7280;
}

.arrow-icon.expanded {
  transform: rotate(180deg);
}

.workflow-body {
  padding: 16px;
  background: #fcfcfc;
}

.step-item {
  display: flex;
  position: relative;
  padding-bottom: 16px;
}

.step-item:last-child {
  padding-bottom: 0;
}

.step-indicator {
  margin-right: 12px;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 12px;
}

.step-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #d1d5db;
  z-index: 2;
  margin-top: 3px;
  box-shadow: 0 0 0 2px #fff;
}

.step-dot.processing {
  background: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

.step-dot.finish {
  background: #10b981;
}

.step-dot.error {
  background: #ef4444;
}

.step-dot.reasoning {
  background: #8b5cf6;
}

.step-line {
  position: absolute;
  top: 12px;
  bottom: -20px;
  width: 2px;
  background: #e0e0e0;
  z-index: 1;
  left: 50%;
  transform: translateX(-50%);
}

.step-content {
  flex: 1;
}

.step-title {
  color: #4b5563;
}

.reasoning-text {
  margin-top: 4px;
  padding: 8px;
  background: #f3f4f6;
  border-radius: 4px;
  color: #6b7280;
  font-family: monospace;
  font-size: 12px;
  white-space: pre-wrap;
}

/* === 输入框区域悬浮优化 === */
.input-container {
  padding: 20px 24px 24px;
  background: transparent;
}

.input-wrapper {
  position: relative;
  max-width: 900px;
  margin: 0 auto;
}

.input-wrapper .ant-input {
  padding-right: 50px;
}

.chat-content {
  background-color: #ffffff;
  /* 悬浮阴影 */
  box-shadow:
    0 6px 24px rgba(0, 0, 0, 0.08),
    0 0 1px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(0, 0, 0, 0.05);
  border-radius: 16px;
  padding: 16px;
  padding-right: 60px;
  resize: none;
  width: 100%;
  height: 120px;
  min-height: 120px;
  max-height: 120px;
  overflow: auto;
  transition: all 0.3s ease;
  font-size: 15px;
}

.chat-content:focus,
.chat-content:hover {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  border-color: rgba(24, 144, 255, 0.3);
}

.input-actions {
  position: absolute;
  bottom: 12px;
  right: 12px;
}

.sent-message-btn {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-color: transparent;
  box-shadow: 0 4px 10px rgba(24, 144, 255, 0.3);
  transition: all 0.3s;
}

.sent-message-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 14px rgba(24, 144, 255, 0.4);
}

/* 右侧内容展示区域 */
.content-section {
  flex: 3;
  display: flex;
  flex-direction: column;
  background: white;
  padding: 16px;
  overflow: hidden;
}

/* 标签内容容器 */
.tab-content {
  flex: 1;
  position: relative;
  overflow: auto;
  border-radius: 8px;
  border: 1px solid oklch(0.928 0.006 264.531);
}

/* 显示标签内容 */
.display-content {
  display: flex;
  flex-direction: column;
}

.preview-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #999;
}

.placeholder-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.preview-placeholder p {
  font-size: 14px;
  color: #999;
}

.preview-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #666;
}

.preview-loading p {
  margin-top: 16px;
  font-size: 14px;
}

.preview-iframe {
  width: 100%;
  height: 100%;
  border: none;
}

.selected-element-alert {
  margin: 0 16px;
}

/* 代码标签内容 */
.code-content {
  padding: 0;
  /* 去掉 padding，配合新 Tabs 样式 */
  display: flex;
  flex-direction: column;
}

.code-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #999;
}

/* 设置标签内容 */
.settings-content {
  padding: 24px;
}

.settings-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #999;
}

.editor-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

/* === Tabs 优化 === */
.tabs {
  background: #f0f2f5;
  padding: 0;
  gap: 1px;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  user-select: none;
  overflow-x: auto;
  flex-wrap: nowrap;
}

.tab {
  padding: 10px 20px;
  background: #e6e8eb;
  color: #666;
  border-radius: 0;
  font-size: 12px;
  transition: all 0.2s;
  border-right: 1px solid rgba(0, 0, 0, 0.05);
  cursor: pointer;
  margin-right: 0;
  display: flex;
  align-items: center;
  white-space: nowrap;
  flex-shrink: 0;
}

.tab:hover {
  background: #f5f5f5;
}

.tab.active {
  background: #ffffff;
  color: #1890ff;
  border-top: 2px solid #1890ff;
  font-weight: 500;
}

.tab.generating {
  background: #e6f7ff;
  color: #1890ff;
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {

  0%,
  100% {
    opacity: 1;
  }

  50% {
    opacity: 0.7;
  }
}

/* === 全局滚动条美化 === */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: #d9d9d9;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #bfbfbf;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .main-content {
    flex-direction: column;
  }

  .chat-section,
  .content-section {
    flex: none;
    height: 50vh;
  }

  .header-center {
    flex-direction: column;
    gap: 8px;
  }

  .action-buttons {
    margin-left: 0;
    padding-left: 0;
    border-left: none;
  }
}

@media (max-width: 768px) {
  .custom-header {
    flex-wrap: wrap;
    padding: 12px 16px;
    gap: 8px;
  }

  .header-center {
    order: 3;
    width: 100%;
    justify-content: flex-start;
  }

  .tab-buttons {
    flex-wrap: wrap;
  }

  .project-name {
    max-width: 120px;
    font-size: 14px;
  }

  .main-content {
    padding: 8px;
    gap: 8px;
  }

  .message-content {
    max-width: 85%;
  }

  .header-right {
    flex-wrap: wrap;
  }

  /* 选中元素信息样式 */
  .selected-element-alert {
    margin: 0 16px;
  }

  .selected-element-info {
    line-height: 1.4;
  }

  .element-header {
    margin-bottom: 8px;
  }

  .element-details {
    margin-top: 8px;
  }

  .element-item {
    margin-bottom: 4px;
    font-size: 13px;
  }

  .element-item:last-child {
    margin-bottom: 0;
  }

  .element-tag {
    font-family: 'Monaco', 'Menlo', monospace;
    font-size: 14px;
    font-weight: 600;
    color: #007bff;
  }

  .element-id {
    color: #28a745;
    margin-left: 4px;
  }

  .element-class {
    color: #ffc107;
    margin-left: 4px;
  }

  .element-selector-code {
    font-family: 'Monaco', 'Menlo', monospace;
    background: #f6f8fa;
    padding: 2px 4px;
    border-radius: 3px;
    font-size: 12px;
    color: #d73a49;
    border: 1px solid #e1e4e8;
  }

  /* 编辑模式按钮样式 */
  .edit-mode-active {
    background-color: #52c41a !important;
    border-color: #52c41a !important;
    color: white !important;
  }

  .edit-mode-active:hover {
    background-color: #73d13d !important;
    border-color: #73d13d !important;
  }
}
</style>
