<template>
  <div class="basic-table-wrapper">
    <div class="basic-table-container">
      <div class="table-toolbar">

        <!-- <div class="toolbar-left">
          <slot name="headerTitle">
            <h3 class="title">{{ title }}</h3>
          </slot>
        </div> -->

        <div class="toolbar-center">
          <div class="custom-search-bar">
            <a-input v-model:value="searchParams.keyword" :placeholder="searchText || '请输入搜索内容'" allow-clear
              style="width: 200px" @pressEnter="handleSearch" size="large" />

            <slot name="searchExtend" :model="searchParams"></slot>

            <div class="search-btns">
              <a-button type="primary" @click="handleSearch">查询</a-button>
              <a-button @click="handleReset" style="margin-left: 8px">重置</a-button>
            </div>
          </div>
        </div>

        <div class="toolbar-right">
          <a-space>
            <slot name="toolbar"></slot>
            <a-button @click="refresh">
              <ReloadOutlined />
            </a-button>
          </a-space>
        </div>
      </div>

      <a-table ref="tableRef" :columns="columns" :dataSource="dataSource" :loading="loading"
        :pagination="paginationProps" @change="handleTableChange" v-bind="$attrs">
        <template #bodyCell="scope">
          <slot v-if="scope.column.dataIndex" :name="scope.column.dataIndex" v-bind="scope"></slot>
          <template v-else>{{ scope.text }}</template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { ReloadOutlined } from '@ant-design/icons-vue';
import type { TablePaginationConfig } from 'ant-design-vue';
import type { TableColumn, PageOption } from './types';

interface Props {
  columns: TableColumn[];
  dataSource: any[];
  loading?: boolean;
  pagination?: PageOption | boolean;
  title?: string;
  searchText?: string;
  showToolbar?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  title: '列表数据',
  showToolbar: true,
  pagination: () => ({ current: 1, pageSize: 10, total: 0 }),
});

const emit = defineEmits(['change', 'register', 'refresh']);

const searchParams = ref<Record<string, any>>({
  keyword: ''
});

const paginationProps = computed(() => {
  if (props.pagination === false) return false;
  return {
    current: (props.pagination as PageOption).current,
    pageSize: (props.pagination as PageOption).pageSize,
    total: (props.pagination as PageOption).total,
    showTotal: (total: number) => `共 ${total} 条数据`,
    showSizeChanger: true,
    pageSizeOptions: ['10', '20', '50', '100'],
  } as TablePaginationConfig;
});

const emitChange = (pag: TablePaginationConfig) => {
  console.log(searchParams.value);
  
  emit('change', {
    page: pag.current,
    pageSize: pag.pageSize,
    ...searchParams.value,
  });
};

const handleSearch = () => {
  const newPagination = { ...paginationProps.value, current: 1 };
  emitChange(newPagination as TablePaginationConfig);
};

const handleReset = () => {
  for (const key in searchParams.value) {
    searchParams.value[key] = undefined;
  }
  searchParams.value = { keyword: '' };
  const newPagination = { ...paginationProps.value, current: 1};
  emitChange(newPagination as TablePaginationConfig);
};

const handleTableChange = (pag: TablePaginationConfig) => {
  emitChange(pag);
};

const refresh = () => {
  emitChange(paginationProps.value as TablePaginationConfig);
};

defineExpose({ refresh });
</script>

<style scoped>
.basic-table-container {
  background: #fff;
  padding: 16px;
  border-radius: 4px;
}

/* 布局容器 */
.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  gap: 16px;
  flex-wrap: wrap;
}

.toolbar-left,
.toolbar-right {
  flex-shrink: 0;
}

.title {
  margin: 0;
  font-weight: 600;
  font-size: 16px;
}

.toolbar-center {
  flex: 1;
  display: flex;
  /* justify-content: center;  */
}

.custom-search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.label {
  font-size: 14px;
  color: #606266;
}

.search-btns {
  display: flex;
  align-items: center;
}

.custom-search-bar :deep(.ant-input),
.custom-search-bar :deep(.ant-select-selector) {
  vertical-align: middle;
}
</style>