<template>
  <div class="page-container">
    <div class="search-bar">
      <div class="search-left">
        <a-input-search v-model:value="searchParams.modelName" placeholder="搜索模型名称 / 编码" style="width: 300px"
          @search="onSearch" allow-clear />
        <a-radio-group v-model:value="searchParams.modelType" button-style="solid" @change="onSearch">
          <a-radio-button value="">全部</a-radio-button>
          <a-radio-button value="CHAT">通用对话</a-radio-button>
          <a-radio-button value="SIMPLE">简单任务</a-radio-button>
          <a-radio-button value="CODE_GEN">代码生成</a-radio-button>
          <a-radio-button value="CODE_AUDIT">代码审计</a-radio-button>
          <a-radio-button value="IMAGE_COLLECT">图片收集</a-radio-button>
          <a-radio-button value="EMBEDDING">知识库向量</a-radio-button>
        </a-radio-group>
      </div>
      <a-button type="primary" size="large" @click="handleEdit(null)">
        <template #icon>
          <PlusOutlined />
        </template>
        接入新模型
      </a-button>
    </div>

    <a-spin :spinning="loading">
      <a-list :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 3, xl: 4, xxl: 5 }" :data-source="dataList"
        :pagination="pagination">
        <template #renderItem="{ item }">
          <a-list-item>
            <a-card hoverable class="model-card">
              <template #extra>
                <a-switch :checked="item.status === 1" checked-children="启用" un-checked-children="禁用"
                  :loading="item.statusLoading" @change="(checked: any) => handleStatusChange(item, checked)" />
              </template>

              <a-card-meta>
                <template #avatar>
                  <a-avatar shape="square" size="large"
                    :style="{ backgroundColor: getProviderColor(item.provider), verticalAlign: 'middle', color: '#fff' }">
                    {{ item.provider ? item.provider.substring(0, 1) : 'A' }}
                  </a-avatar>
                </template>
                <template #title>
                  <div class="card-title">
                    <span class="truncate" :title="item.modelName">{{ item.modelName }}</span>
                  </div>
                </template>
                <template #description>
                  <div class="card-desc">
                    <a-tag :color="getTypeColor(item.modelType)">{{ getTypeLabel(item.modelType) }}</a-tag>
                    <a-tag>{{ item.provider }}</a-tag>
                  </div>
                </template>
              </a-card-meta>

              <div class="card-info">
                <div class="info-row">
                  <span>Code:</span>
                  <a-typography-text copyable class="code-text">{{ item.modelCode }}</a-typography-text>
                </div>
                <div class="info-row">
                  <span>Context:</span>
                  <span class="font-medium">{{ item.maxTokens }} token</span>
                </div>
                <div class="info-row">
                  <span>费率(In/Out):</span>
                  <span class="price-text">
                    ${{ item.inputPrice }}/${{ item.outputPrice }}
                  </span>
                </div>
              </div>

              <template #actions>
                <a-tooltip title="配置参数">
                  <SettingOutlined key="setting" @click="handleEdit(item)" />
                </a-tooltip>
                <a-tooltip title="密钥管理">
                  <KeyOutlined key="key" @click="goKeyManage(item.provider)" />
                </a-tooltip>
                <a-popconfirm title="确认删除该模型？" @confirm="handleDelete(item.id)">
                  <DeleteOutlined key="delete" class="delete-icon" />
                </a-popconfirm>
              </template>
            </a-card>
          </a-list-item>
        </template>
      </a-list>
    </a-spin>

    <a-modal v-model:open="modalVisible" :title="currentId ? '编辑模型' : '接入新模型'" @ok="handleModalOk"
      :confirmLoading="modalLoading">
      <a-form layout="vertical" ref="formRef" :model="formState">
        <a-form-item label="模型名称" name="modelName" required>
          <a-input v-model:value="formState.modelName" placeholder="如: GPT-4 Turbo" />
        </a-form-item>
        <a-form-item label="模型编码 (调用传参)" name="modelCode" required>
          <a-input v-model:value="formState.modelCode" placeholder="如: gpt-4-1106-preview" />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="供应商" name="provider" required>
              <a-select v-model:value="formState.provider" placeholder="请选择模型供应商">
                <a-select-option v-for="item in AI_PROVIDER_OPTIONS" :key="item.value" :value="item.value">
                  {{ item.label }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="类型" name="modelType" required>
              <a-select v-model:value="formState.modelType" placeholder="请选择模型类型">
                <a-select-option value="SIMPLE">简单任务</a-select-option>
                <a-select-option value="CHAT">通用对话</a-select-option>
                <a-select-option value="CODE_GEN">代码生成</a-select-option>
                <a-select-option value="CODE_AUDIT">代码审计</a-select-option>
                <a-select-option value="IMAGE_COLLECT">图片收集</a-select-option>
                <a-select-option value="EMBEDDING">知识库向量</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="接口地址 (Base URL)" name="baseUrl" required>
          <a-input v-model:value="formState.baseUrl" placeholder="https://api.openai.com/v1" />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="最大Token" name="maxTokens">
              <a-input-number v-model:value="formState.maxTokens" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <!-- <a-form-item label="输入价格($/1k)" name="inputPrice">
                <a-input-number v-model:value="formState.inputPrice" :step="0.001" style="width: 100%" />
             </a-form-item> -->
          </a-col>
        </a-row>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { PlusOutlined, SettingOutlined, DeleteOutlined, KeyOutlined } from '@ant-design/icons-vue';
import { AI_PROVIDER_OPTIONS } from '@/enums/ai';
import {
  listAiModelByPage,
  addAiModel,
  updateAiModel,
  deleteAiModel,
} from '@/api/aiModelController';

const loading = ref(false);
const modalLoading = ref(false);
const dataList = ref<API.AiModel[]>([]);

// 查询参数
const searchParams = reactive({
  current: 1,
  pageSize: 12,
  modelName: '',
  modelType: ''
});

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 12,
  total: 0,
  onChange: (page: number, size: number) => {
    searchParams.current = page;
    searchParams.pageSize = size;
    fetchData();
  }
});

// 表单数据
const modalVisible = ref(false);
const currentId = ref<string | null>(null);
const formState = reactive<Partial<API.AiModel>>({
  provider: undefined,
  modelType: undefined,
  maxTokens: 4096,
  inputPrice: 0,
  outputPrice: 0,
  status: 1
});

// 获取数据
const fetchData = async () => {
  loading.value = true;

  const paramsToSend: Record<string, any> = {};
  for (const key in searchParams) {
    const value = (searchParams as any)[key];
    if (value !== null && value !== undefined && value !== '') {
      paramsToSend[key] = value;
    }
  }

  try {
    const res = await listAiModelByPage(paramsToSend);

    if (res.data.data) {
      dataList.value = res.data.data.records ?? []
      pagination.total = Number(res.data.data.totalRow) ?? 0
    } else {
      message.error(res.data.message || '获取数据失败');
    }
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const onSearch = () => {
  searchParams.current = 1;
  fetchData();
};

// 状态切换 (启用/禁用)
const handleStatusChange = async (item: any, checked: boolean) => {
  item.statusLoading = true;
  try {
    const res = await updateAiModel({
      id: item.id,
      status: checked ? 1 : 0
    });

    if (res.data.code === 0) {
      item.status = checked ? 1 : 0;
      message.success('状态已更新');
    } else {
      message.error(res.data.message || '操作失败');
    }
  } catch (error) {
    // 失败回调，界面开关回弹（可选）
  } finally {
    item.statusLoading = false;
  }
};

// 删除模型
const handleDelete = async (id: string) => {
  try {
    const res = await deleteAiModel({ id: id as unknown as number });
    if (res.data.code === 0) {
      message.success('删除成功');
      fetchData(); // 刷新列表
    } else {
      message.error(res.data.message);
    }
  } catch (error) {
    message.error('删除请求失败');
  }
};

// 新增/编辑 模态框逻辑
const handleEdit = (item: API.AiModel | null) => {
  currentId.value = item?.id as unknown as string || null;
  modalVisible.value = true;

  if (item) {
    Object.assign(formState, item);
  } else {
    Object.assign(formState, {
      modelName: '',
      modelCode: '',
      provider: undefined,
      modelType: undefined,
      baseUrl: '',
      maxTokens: 4096,
      inputPrice: 0,
      outputPrice: 0,
      status: 1
    });
  }
};

// 提交表单
const handleModalOk = async () => {
  if (!formState.modelName || !formState.modelCode || !formState.baseUrl) {
    message.warning('请填写必填项');
    return;
  }

  modalLoading.value = true;
  try {
    let res;
    if (currentId.value) {
      // 更新
      res = await updateAiModel({ ...formState, id: currentId.value });
    } else {
      // 新增
      res = await addAiModel(formState);
    }

    if (res.data.code === 0) {
      message.success(currentId.value ? '更新成功' : '创建成功');
      modalVisible.value = false;
      fetchData();
    } else {
      message.error(res.data.message);
    }
  } catch (error) {
    console.error(error);
  } finally {
    modalLoading.value = false;
  }
};

const goKeyManage = (provider: string) => {
  message.info(`跳转到密钥管理页面，筛选 Provider: ${provider}`);
  // router.push({ path: '/ai/keys', query: { provider } });
};

// --- 视觉样式辅助函数 ---
const getProviderColor = (p: string) => {
  const map: Record<string, string> = {
    'OpenAI': '#10a37f', 'DeepSeek': '#4e54c8', 'GLM': '#0078d4',
    'Claude': '#d97757', 'Google': '#4285F4', 'Aliyun': '#ff6a00'
  };
  return map[p] || '#8c8c8c';
};

const getTypeColor = (t: string) => {
  const map: Record<string, string> = {
    'CHAT': 'blue', 'SIMPLE': 'yellow', 'CODE_GEN': 'cyan', 'CODE_AUDIT': 'orange',
    'IMAGE_COLLECT': 'purple', 'EMBEDDING': 'green'
  };
  return map[t] || 'default';
};

const getTypeLabel = (t: string) => {
  const map: Record<string, string> = {
    'CHAT': '通用对话', 'SIMPLE': '简单任务', 'CODE_GEN': '代码生成', 'CODE_AUDIT': '代码审计',
    'IMAGE_COLLECT': '图片收集', 'EMBEDDING': '知识库向量'
  };
  return map[t] || t;
};

// 初始化
onMounted(() => {
  fetchData();
});
</script>

<style scoped>
/* 样式保持不变，复用您之前的 CSS */
.search-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  background-color: #fff;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
}

.search-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.model-card {
  transition: transform 0.3s;
  border-radius: 8px;
}

.model-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.truncate {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-desc {
  display: flex;
  gap: 8px;
  margin-top: 4px;
}

.card-info {
  margin-top: 16px;
  color: #6b7280;
  font-size: 12px;
  background-color: #f9fafb;
  padding: 8px;
  border-radius: 4px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.price-text {
  color: #f97316;
}

.delete-icon {
  color: #ef4444;
}
</style>