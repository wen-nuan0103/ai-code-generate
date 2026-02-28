<template>
  <div class="page-container">
    <div class="content-box">
      <div class="toolbar">
        <!-- <span class="title">提示词工程 (Prompt Engineering)</span> -->
        <a-button type="primary" @click="handleEdit(null)">新建模板</a-button>
      </div>

      <a-table :columns="columns" :data-source="data" :loading="loading" row-key="id">
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'action'">
            <a @click="handleEdit(record)">调优</a>
            <a-divider type="vertical" />
            <a class="delete-text">删除</a>
          </template>
        </template>
      </a-table>
    </div>

    <a-drawer
      v-model:open="drawerVisible"
      :title="currentId ? 'Prompt 调试与编辑' : '新建 Prompt 模板'"
      width="800"
      :footer-style="{ textAlign: 'right' }"
      class="prompt-drawer"
    >
      <div class="drawer-content">
        <a-card size="small" title="基础信息" class="section-card">
          <a-form layout="inline">
            <a-form-item label="模板编码"><a-input v-model:value="form.code" placeholder="CODE_GEN_V1" /></a-form-item>
            <a-form-item label="模板名称"><a-input v-model:value="form.name" placeholder="代码生成主逻辑" /></a-form-item>
            <a-form-item label="默认模型">
              <a-select v-model:value="form.modelId" style="width: 150px">
                <a-select-option :value="1">GPT-4 Turbo</a-select-option>
              </a-select>
            </a-form-item>
          </a-form>
        </a-card>

        <div class="prompt-grid">
          <div class="grid-col">
            <span class="section-label">System Prompt (系统预设)</span>
            <a-textarea 
              v-model:value="form.systemMessage" 
              class="flex-textarea" 
              placeholder="你是一个资深的 Java 架构师..." 
            />
          </div>
          <div class="grid-col">
            <span class="section-label">User Prompt (用户输入)</span>
            <div class="sub-label">支持变量: <span v-pre>{{input}}</span></div>
            <a-textarea 
              v-model:value="form.userMessage" 
              class="flex-textarea" 
              placeholder="请帮我生成一个 {{requirement}} 模块..." 
            />
          </div>
        </div>

        <a-card size="small" title="参数配置" class="section-card">
          <a-row :gutter="16">
            <a-col :span="12">
              <span>温度 (Temperature): {{ form.parameters.temperature }}</span>
              <a-slider v-model:value="form.parameters.temperature" :min="0" :max="2" :step="0.1" />
            </a-col>
            <a-col :span="12">
              <div class="test-btn-wrapper">
                <a-button type="dashed" @click="handleTestRun">⚡ 试运行</a-button>
              </div>
            </a-col>
          </a-row>
        </a-card>
      </div>

      <template #footer>
        <a-button style="margin-right: 8px" @click="drawerVisible = false">取消</a-button>
        <a-button type="primary" @click="handleSave">保存模板</a-button>
      </template>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';

const loading = ref(false);
const data = ref<any[]>([]);
const drawerVisible = ref(false);
const currentId = ref<number | null>(null);

const columns = [
  { title: '编码', dataIndex: 'code', width: 150 },
  { title: '名称', dataIndex: 'name', width: 200 },
  { title: '建议模型', dataIndex: 'modelName', width: 150 },
  { title: '最后更新', dataIndex: 'updateTime' },
  { title: '操作', dataIndex: 'action', width: 150 },
];

const form = reactive({
  code: '',
  name: '',
  modelId: 1,
  systemMessage: '',
  userMessage: '',
  parameters: { temperature: 0.7 }
});

const fetchData = () => {
  loading.value = true;
  setTimeout(() => {
    data.value = [
      { id: 1, code: 'JAVA_CRUD', name: 'Java CRUD 生成器', modelName: 'GPT-4 Turbo', updateTime: '2026-01-09 12:00' }
    ];
    loading.value = false;
  }, 500);
};

const handleEdit = (record: any) => {
  currentId.value = record?.id || null;
  if (record) {
    // 模拟回显
    form.code = record.code;
    form.name = record.name;
    form.systemMessage = '你是一个Java专家...';
    form.userMessage = '生成代码...';
  } else {
    form.code = ''; form.name = ''; form.systemMessage = ''; form.userMessage = '';
  }
  drawerVisible.value = true;
};

const handleTestRun = () => {
  message.loading('正在调用 AI 进行测试...', 2);
};

const handleSave = () => {
  message.success('保存成功');
  console.log(form);
  drawerVisible.value = false;
  fetchData();
};

onMounted(() => fetchData());
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
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: bold;
}

.delete-text {
  color: #ef4444;
}

/* Drawer 内部布局 */
.drawer-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  gap: 16px;
}

.section-card {
  margin-bottom: 0;
}

.prompt-grid {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  min-height: 0; /* 防止溢出 */
}

.grid-col {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.section-label {
  font-weight: bold;
}

.sub-label {
  font-size: 12px;
  color: #9ca3af; /* text-gray-400 */
  margin-bottom: 4px;
}

.flex-textarea {
  flex: 1;
  resize: none;
  height: 300px; /* 或者使用 100% 配合父级高度 */
}

.test-btn-wrapper {
  text-align: right;
  margin-top: 8px;
}
</style>