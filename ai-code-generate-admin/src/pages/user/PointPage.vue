<template>
    <div class="page-wrapper p-4">
        <BasicTable title="积分流水记录" :columns="columns" :dataSource="data" :loading="loading" :pagination="searchParams"
            search-text="输入用户账号" @change="handleTableChange" @refresh="fetchData">
            <template #searchExtend="{ model }">
                <span class="ml-3">类型：</span>
                <a-select v-model:value="model.type" placeholder="全部类型" style="width: 140px" allow-clear size="large">
                    <a-select-option :value="1">注册赠送</a-select-option>
                    <a-select-option :value="2">邀请奖励</a-select-option>
                    <a-select-option :value="3">每日签到</a-select-option>
                    <a-select-option :value="4">充值购买</a-select-option>
                    <a-select-option :value="5">失败退款</a-select-option>
                    <a-select-option :value="6">系统补偿</a-select-option>
                    <a-select-option :value="7">系统扣除</a-select-option>
                    <a-select-option :value="101">AI生成消耗</a-select-option>
                    <a-select-option :value="102">应用发布消耗</a-select-option>
                </a-select>

                <span class="ml-3">日期：</span>
                <a-range-picker size="large" v-model:value="model.dateRange" value-format="YYYY-MM-DD"
                    style="width: 240px" />
            </template>

            <template #toolbar>
                <a-button type="primary" @click="handleManualCharge">
                    <template #icon>
                        <PayCircleOutlined />
                    </template>
                    人工充值/扣款
                </a-button>
                <a-button>导出报表</a-button>
            </template>

            <template #userInfo="{ record }">
                <a-space>
                    <a-avatar :src="record.avatar" size="small" />
                    <div style="display: flex; flex-direction: column; line-height: 1.2;">
                        <span>{{ record.userName || '未知用户' }}</span>
                        <span style="font-size: 12px; color: #999;">ID: {{ record.userId }}</span>
                    </div>
                </a-space>
            </template>

            <template #amount="{ record }">
                <span :style="{ color: record.isIncome ? '#52c41a' : '#ff4d4f', fontWeight: 'bold' }">
                    {{ record.amount > 0 ? '+' : '' }}{{ record.amount }}
                </span>
            </template>

            <template #type="{ record }">
                <a-tag :color="record.isIncome ? 'green' : 'volcano'">
                    {{ getTypeLabel(record.type) }}
                </a-tag>
            </template>

            <template #bizNo="{ text }">
                <a-typography-text style="width: 180px; display: block;" :content="text" :ellipsis="{ tooltip: true }"
                    copyable />
            </template>

        </BasicTable>

        <a-modal v-model:open="modalVisible" title="人工调整积分" @ok="submitCharge" :confirmLoading="modalLoading">
            <a-form :model="chargeForm" layout="vertical">
                <a-form-item label="目标用户" name="userId" :rules="[{ required: true, message: '请选择目标用户' }]">
                    <a-select size="large" v-model:value="chargeForm.userId" show-search placeholder="输入账号搜索"
                        :default-active-first-option="false" :filter-option="false"
                        :not-found-content="searchState.fetching ? undefined : null" :options="searchState.data"
                        @search="handleSearchUser" @change="handleChangeUser" style="width: 100%">
                        <template v-if="searchState.fetching" #notFoundContent>
                            <a-spin size="small" />
                        </template>

                        <template #option="{ value: val, label, record }">
                            <div style="display: flex; justify-content: space-between;">
                                <span>{{ record.userAccount }}</span>
                                <span style="color: #999">{{ record.userName }}</span>
                            </div>
                        </template>
                    </a-select>
                </a-form-item>
                <a-form-item label="变动金额" required help="正数为增加，负数为扣除">
                    <a-input-number size="large" v-model:value="chargeForm.amount" style="width: 100%"
                        placeholder="例如: 100 或 -50" />
                </a-form-item>
                <a-form-item label="备注原因" required>
                    <a-textarea v-model:value="chargeForm.remark" placeholder="例如：用户投诉生成失败，人工补发" />
                </a-form-item>
            </a-form>
        </a-modal>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { debounce } from 'lodash-es'
import { PayCircleOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import BasicTable from '@/components/table/BaseTable.vue';
import type { TableColumn } from '@/components/table/types';
import { chargePoints, listPointsLogVoByPage } from '@/api/pointsController';
import { listUserVoByPage } from '@/api/userController'

// 列定义
const columns: TableColumn[] = [
    {
        title: '流水ID',
        dataIndex: 'id',
        width: 200,
        ellipsis: true
    },
    {
        title: '用户',
        dataIndex: 'userInfo',
        width: 150
    },
    {
        title: '类型',
        dataIndex: 'type',
        width: 100,
        align: 'center'
    },
    {
        title: '变动金额',
        dataIndex: 'amount',
        width: 120,
        align: 'center'
    },
    {
        title: '变动后余额',
        dataIndex: 'currentPoints',
        width: 120,
        align: 'center'
    },
    {
        title: '业务单号',
        dataIndex: 'bizNo',
        width: 200,
    },
    {
        title: '备注',
        dataIndex: 'remark',
        width: 200,
        ellipsis: true
    },
    {
        title: '时间',
        dataIndex: 'createTime',
        width: 170,
        align: 'center'
    },
];

// 表格数据
const data = ref<API.PointsVO[]>([]);
const total = ref(0)
const loading = ref(false);

const initSearchParams = {
    current: 1,
    pageSize: 10,
    total: 0,
    userAccount: undefined,
    type: undefined,
    createTimeStart: undefined,
    createTimeEnd: undefined
};
const searchParams = reactive({
    ...initSearchParams
})


// 辅助函数：类型映射
const getTypeLabel = (type: number) => {
    const map: Record<number, string> = {
        1: '注册赠送', 2: '邀请奖励', 3: '每日签到', 4: '充值购买',5:'失败退款',6:'系统补偿 (增加)',7:'系统补偿（扣除',
        101: 'AI生成', 102: '应用发布'
    };
    return map[type] || '未知';
};

const copyToClip = (text: string) => {
    navigator.clipboard.writeText(text);
    message.success('复制成功');
};

// 数据获取逻辑
const fetchData = async () => {
    loading.value = true;
    const paramsToSend: Record<string, any> = {};
    for (const key in searchParams) {
        const value = (searchParams as any)[key];
        if (key === 'dateRange' && Array.isArray(value) && value.length === 2) {
            paramsToSend['createTimeStart'] = value[0] + ' 00:00:00';
            paramsToSend['createTimeEnd'] = value[1] + ' 23:59:59';
            continue;
        }
        if (value !== null && value !== undefined && value !== '') {
            paramsToSend[key] = value;
        }
    }

    try {
        const res = await listPointsLogVoByPage(paramsToSend)
        if (res.data.data) {
            data.value = res.data.data.records ?? []
            total.value = res.data.data.totalRow ?? 0
        } else {
            message.error('获取数据失败，' + res.data.message)
        }
    } finally {
        loading.value = false;
    }
};

const handleTableChange = (pag: any) => {
    console.log(pag);

    const { keyword, ...restParams } = pag;
    Object.assign(searchParams, initSearchParams, {
        ...restParams,
        userAccount: keyword
    });

    fetchData();
}

// 人工充值逻辑
const modalVisible = ref(false);
const modalLoading = ref(false);
const searchState = reactive({
    data: [] as any[],
    fetching: false,
});
const chargeForm = reactive({ userId: undefined, amount: undefined, remark: '' });

const handleManualCharge = () => {
    chargeForm.userId = undefined;
    chargeForm.amount = undefined;
    chargeForm.remark = '';
    searchState.data = [];
    modalVisible.value = true;
};

const submitCharge = async () => {
    if (!chargeForm.userId || !chargeForm.amount) {
        return message.warning('请填写完整信息');
    }
    modalLoading.value = true;
    try {
        await chargePoints(chargeForm);
        // console.log(chargeForm);
        message.success('操作成功');
        modalVisible.value = false;
        fetchData(); // 刷新列表
    } catch (e) {

    } finally {
        modalLoading.value = false;
    }
};


const handleSearchUser = debounce(async (value: string) => {
    if (!value) {
        searchState.data = [];
        return;
    }

    searchState.fetching = true;
    try {
        const res = await listUserVoByPage({
            userAccount: value,
            pageSize: 10,
            current: 1
        });

        if (res.data.data && res.data.data.records) {
            searchState.data = res.data.data.records.map((user: any) => ({
                label: user.userAccount,
                value: user.id,
                record: user
            }));
        } else {
            searchState.data = [];
        }
    } finally {
        searchState.fetching = false;
    }
}, 500);

const handleChangeUser = (val: any) => {
    chargeForm.userId = val;
};

onMounted(() => {
    fetchData();
});
</script>