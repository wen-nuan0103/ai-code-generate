<template>
  <div class="page-container">
    <div class="content-box">
      <div class="toolbar">
        <div class="search-group">
          <a-select v-model:value="searchParams.provider" placeholder="全部供应商" style="width: 150px" allow-clear
            @change="handleSearch">
            <a-select-option v-for="item in AI_PROVIDER_OPTIONS" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
          <a-input v-model:value="searchParams.key" placeholder="输入 Key 后四位搜索" style="width: 200px"
            @pressEnter="handleSearch" allow-clear />
          <a-button type="primary" @click="handleSearch">查询</a-button>
        </div>
        <a-button type="primary" @click="handleAdd">
          <template #icon>
            <PlusOutlined />
          </template> 新增密钥
        </a-button>
      </div>

      <a-table :columns="columns" :data-source="data" :loading="loading" :pagination="pagination" row-key="id"
        @change="handleTableChange">
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'accessKey'">
            <a-typography-text copyable :content="record.accessKey">
              <span class="key-text">
                {{ record.accessKey.replace(/^(.{6})(.*)(.{4})$/, '$1******$3') }}
              </span>
            </a-typography-text>
          </template>

          <template v-else-if="column.dataIndex === 'status'">
            <a-tag :color="record.status === 1 ? 'success' : 'error'">
              {{ record.status === 1 ? '正常' : '失效' }}
            </a-tag>
          </template>

          <template v-else-if="column.dataIndex === 'errorCount'">
            <span :class="{ 'error-text': record.errorCount > 0 }">
              {{ record.errorCount }}
            </span>
          </template>

          <template v-else-if="column.dataIndex === 'action'">
            <a-space>
              <a @click="handleCheck(record)">验证</a>
              <a-popconfirm title="确定删除该密钥?" ok-text="是" cancel-text="否" @confirm="handleDelete(record.id)">
                <a class="delete-text">删除</a>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <a-modal v-model:open="modalVisible" title="添加 API Key" @ok="handleSubmit" :confirmLoading="confirmLoading">
      <a-form layout="vertical">
        <a-form-item label="供应商" required>
          <a-select v-model:value="form.provider">
            <a-select-option v-for="item in AI_PROVIDER_OPTIONS" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="Access Key (sk-...)" required help="支持批量添加，请每行输入一个 Key">
          <a-textarea v-model:value="form.accessKey" :rows="5"
            placeholder="sk-xxxxxxxxxxxxxxxxxxxx&#10;sk-yyyyyyyyyyyyyyyyyyyy" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { PlusOutlined } from '@ant-design/icons-vue';
import { AI_PROVIDER_OPTIONS } from '@/enums/ai';
import { listAiApiKeyByPage, addAiApiKey } from '@/api/aiKeyController';

const loading = ref(false);
const confirmLoading = ref(false);
const modalVisible = ref(false);

// 搜索参数
const searchParams = reactive({
  provider: undefined,
  key: ''
});

const data = ref<any[]>([]);

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: (total: number) => `共 ${total} 条`,
  showSizeChanger: true,
  pageSizeOptions: ['10', '20', '50']
});

const columns = [
  { title: '供应商', dataIndex: 'provider', width: 120 },
  { title: 'Access Key', dataIndex: 'accessKey', width: 300 },
  { title: '状态', dataIndex: 'status', width: 100 },
  { title: '余额', dataIndex: 'balance', width: 100, customRender: ({ text }: any) => text !== null ? `$${Number(text).toFixed(4)}` : '-' },
  { title: '连续报错', dataIndex: 'errorCount', width: 100, align: 'center' },
  { title: '最后使用', dataIndex: 'lastUsedTime', width: 180 },
  { title: '操作', dataIndex: 'action', width: 150, fixed: 'right' },
];

/**
 * 获取数据
 */
const fetchData = async () => {
  loading.value = true;
  try {
    const res = await listAiApiKeyByPage({
      current: pagination.current,
      pageSize: pagination.pageSize,
      ...searchParams
    });

    if (res.data.data) {
      data.value = res.data.data.records ?? []
      pagination.total = Number(res.data.data.totalRow) ?? 0
    } else {
      message.error(res.data.message || '获取数据失败');
    }
  } catch (error) {
    console.error('Fetch error:', error);
    message.error('网络请求失败');
  } finally {
    loading.value = false;
  }
};

/**
 * 触发搜索（重置到第一页）
 */
const handleSearch = () => {
  pagination.current = 1;
  fetchData();
};

/**
 * 表格分页、排序、筛选变化时触发
 */
const handleTableChange = (pag: any) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  fetchData();
};

/**
 * 删除逻辑
 */
const handleDelete = async (id: number) => {
  try {
    // // 假设你有 deleteAiApiKey 接口
    // const res = await deleteAiApiKey({ id }); 
    // if (res.data.code === 0) {
    //   message.success('删除成功');
    //   // 如果当前页只有一条数据且不是第一页，删除后向前翻页
    //   if (data.value.length === 1 && pagination.current > 1) {
    //     pagination.current--;
    //   }
    //   fetchData();
    // } else {
    //   message.error(res.data.message || '删除失败');
    // }
    message.info("删除等待实现")
  } catch (error) {
    message.error('删除操作异常');
  }
};

/**
 * 验证逻辑 (Mock)
 */
const handleCheck = (record: any) => {
  message.loading({ content: '正在验证 Key 有效性...', key: 'check' });
  setTimeout(() => {
    message.success({ content: '验证通过，余额充足', key: 'check' });
  }, 1000);
};

const form = reactive({
  provider: 'OpenAI',
  accessKey: ''
});

const handleAdd = () => {
  form.accessKey = '';
  form.provider = 'OpenAI';
  modalVisible.value = true;
};

const handleSubmit = async () => {
  if (!form.accessKey.trim()) {
    message.warning('请输入 Access Key');
    return;
  }

  confirmLoading.value = true;

  try {
    const res = await addAiApiKey(form)

    if (res.data.code === 0) {
      handleSearch();
      message.success("添加成功")
    } else {
      message.error(res.data.message);
    }
  } catch (error) {
    console.error(error);
    message.error('系统异常');
  } finally {
    confirmLoading.value = false;
    modalVisible.value = false;
  }
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
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
}

.search-group {
  display: flex;
  gap: 8px;
  align-items: center;
}

.key-text {
  font-family: 'Menlo', 'Monaco', 'Courier New', monospace;
  background-color: #f3f4f6;
  padding: 2px 6px;
  border-radius: 4px;
  color: #4b5563;
  font-size: 13px;
}

.error-text {
  color: #ef4444;
  font-weight: bold;
}

.delete-text {
  color: #ff4d4f;
  cursor: pointer;
}

.delete-text:hover {
  color: #cf1322;
}
</style>