<template>
  <div class="page-container">
    <div class="content-box">
      <div class="toolbar">
        <a-form layout="inline" :model="searchParams" @keyup.enter="handleSearch">
          <a-form-item label="Trace ID">
            <a-input v-model:value="searchParams.traceId" placeholder="请输入 Trace ID" style="width: 200px" allow-clear />
          </a-form-item>
          <a-form-item label="用户 ID">
            <a-input v-model:value="searchParams.userId" placeholder="请输入用户 ID" style="width: 150px" allow-clear />
          </a-form-item>
          <a-form-item label="状态">
            <a-select v-model:value="searchParams.status" placeholder="全部" style="width: 100px" allow-clear>
              <a-select-option :value="1">成功</a-select-option>
              <a-select-option :value="2">失败</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item>
            <a-button type="primary" @click="handleSearch" :loading="loading">查询</a-button>
            <a-button style="margin-left: 8px" @click="handleReset">重置</a-button>
          </a-form-item>
        </a-form>
      </div>

      <a-table :columns="columns" :data-source="data" :loading="loading" :pagination="pagination" row-key="id"
        size="small" :scroll="{ x: 1300 }" @change="handleTableChange">
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'traceId'">
            <a-typography-text copyable class="font-mono" style="font-size: 12px;">
              {{ record.traceId }}
            </a-typography-text>
          </template>

          <template v-else-if="column.dataIndex === 'tokens'">
            <a-space size="small">
              <span style="color: #52c41a; display: inline-block; width: 80px;" title="Input Tokens">
                In: {{ record.inputTokens }}
              </span>

              <a-divider type="vertical" />

              <span style="color: #1890ff" title="Output Tokens">
                Out: {{ record.outputTokens }}
              </span>
            </a-space>
          </template>

          <template v-else-if="column.dataIndex === 'totalCost'">
            <span v-if="record.totalCost">¥{{ Number(record.totalCost).toFixed(6) }}</span>
            <span v-else>-</span>
          </template>

          <template v-else-if="column.dataIndex === 'duration'">
            <span :class="{ 'warning-text': record.duration > 1000 }">
              {{ record.duration }}ms
            </span>
          </template>

          <template v-else-if="column.dataIndex === 'status'">
            <a-tag :color="record.status === 1 ? 'success' : 'error'">
              {{ record.status === 1 ? '成功' : '失败' }}
            </a-tag>
          </template>

          <template v-else-if="column.dataIndex === 'errorMsg'">
            <a-tooltip v-if="record.errorMsg" :title="record.errorMsg">
              <span class="error-msg-cell">{{ record.errorMsg }}</span>
            </a-tooltip>
            <span v-else>-</span>
          </template>

          <template v-else-if="column.dataIndex === 'action'">
            <a @click="handleDetail(record)">详情</a>
          </template>
        </template>
      </a-table>
    </div>

    <a-modal v-model:open="detailVisible" title="调用详情" :footer="null" width="600px">
      <a-descriptions bordered :column="1" size="small">
        <a-descriptions-item label="Trace ID">{{ currentLog.traceId }}</a-descriptions-item>
        <a-descriptions-item label="用户 ID">{{ currentLog.userId }}</a-descriptions-item>
        <a-descriptions-item label="模型">{{ currentLog.modelName }}</a-descriptions-item>
        <a-descriptions-item label="输入 Tokens">{{ currentLog.inputTokens }}</a-descriptions-item>
        <a-descriptions-item label="输出 Tokens">{{ currentLog.outputTokens }}</a-descriptions-item>
        <a-descriptions-item label="总成本">¥{{ currentLog.totalCost }}</a-descriptions-item>
        <a-descriptions-item label="耗时">{{ currentLog.duration }} ms</a-descriptions-item>
        <a-descriptions-item label="调用时间">{{ currentLog.createTime }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="currentLog.status === 1 ? 'success' : 'error'">
            {{ currentLog.status === 1 ? '成功' : '失败' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="错误信息" v-if="currentLog.status !== 1">
          <span style="color: red">{{ currentLog.errorMsg }}</span>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { listAiGenerateLogByPage } from '@/api/aiLogController';

const loading = ref(false);
const detailVisible = ref(false);
const currentLog = ref<any>({});
const data = ref<any[]>([]);

// 搜索参数
const searchParams = reactive({
  current: 1,
  pageSize: 15,
  traceId: '',
  userId: '',
  status: undefined as number | undefined
});

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 15,
  total: 0,
  onChange: (page: number, size: number) => {
    searchParams.current = page;
    searchParams.pageSize = size;
    fetchData();
  }
});

const columns = [
  { title: 'Trace ID', dataIndex: 'traceId', width: 130, fixed: 'left' },
  { title: '用户ID', dataIndex: 'userId', width: 150 },
  { title: '模型', dataIndex: 'modelName', width: 120 },
  { title: '输入/输出 (Tokens)', dataIndex: 'tokens', width: 180 },
  { title: '总成本', dataIndex: 'totalCost', width: 120 },
  { title: '耗时', dataIndex: 'duration', width: 100 },
  { title: '状态', dataIndex: 'status', width: 100 },
  { title: '报错信息', dataIndex: 'errorMsg', width: 200, ellipsis: true },
  { title: '时间', dataIndex: 'createTime', width: 180 },
  { title: '操作', dataIndex: 'action', width: 80, fixed: 'right' },
];

/**
 * 加载数据
 */
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
    const res = await listAiGenerateLogByPage(paramsToSend);

    if (res.data.data) {
      data.value = res.data.data.records ?? []
      pagination.total = Number(res.data.data.totalRow) ?? 0
    } else {
      message.error(res.data.message || '获取日志失败');
    }
  } catch (error) {
    console.error(error);
    message.error('网络请求失败');
  } finally {
    loading.value = false;
  }
};

/**
 * 搜索按钮点击
 */
const handleSearch = () => {
  pagination.current = 1; // 搜索时重置到第一页
  fetchData();
};

/**
 * 重置按钮点击
 */
const handleReset = () => {
  searchParams.traceId = '';
  searchParams.userId = '';
  searchParams.status = undefined;
  handleSearch();
};

/**
 * 表格分页、排序变化时触发
 */
const handleTableChange = (pag: any) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  fetchData();
};

/**
 * 查看详情
 */
const handleDetail = (record: any) => {
  currentLog.value = record;
  detailVisible.value = true;
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.content-box {
  background-color: #fff;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
}

.toolbar {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.font-mono {
  font-family: 'Menlo', 'Monaco', 'Courier New', monospace;
}

.warning-text {
  color: #faad14;
  font-weight: 500;
}

.error-msg-cell {
  color: #ff4d4f;
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  cursor: pointer;
}
</style>