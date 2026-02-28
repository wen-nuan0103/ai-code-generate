<template>
    <div id="userManagePage">
        <a-table :columns="columns" :data-source="data" :pagination="pagination" @change="handleTableChange">
            <!-- 搜索表单 -->
            <a-form layout="inline" :model="searchParams" @finish="handleSearch">
                <a-form-item label="账号">
                    <a-input v-model:value="searchParams.userAccount" placeholder="输入账号" />
                </a-form-item>
                <a-form-item label="用户名">
                    <a-input v-model:value="searchParams.userName" placeholder="输入用户名" />
                </a-form-item>
                <a-form-item>
                    <a-button type="primary" html-type="submit">搜索</a-button>
                </a-form-item>
            </a-form>
            <a-divider />
            <!-- 表格 -->
            <template #bodyCell="{ column, record }">
                <template v-if="column.dataIndex === 'avatar'">
                    <a-image :src="record.avatar" :width="120" />
                </template>
                <template v-else-if="column.dataIndex === 'role'">
                    <div v-if="record.role === 'admin'">
                        <a-tag color="green">管理员</a-tag>
                    </div>
                    <div v-else>
                        <a-tag color="blue">普通用户</a-tag>
                    </div>
                </template>
                <template v-else-if="column.dataIndex === 'createTime'">
                    {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
                </template>
                <template v-else-if="column.key === 'action'">
                    <a-button danger>删除</a-button>
                </template>
            </template>

        </a-table>
    </div>
</template>
<script lang="ts" setup>
import { listUserVoByPage } from '@/api/userController';
import { message } from 'ant-design-vue';
import { computed, onMounted, reactive, ref } from 'vue';
import dayjs from 'dayjs';
const columns = [
    {
        title: 'id',
        dataIndex: 'id',
        align:'center'
    },
    {
        title: '账号',
        dataIndex: 'userAccount',
        align:'center'
    },
    {
        title: '用户名',
        dataIndex: 'userName',
        align:'center'
    },
    {
        title: '头像',
        dataIndex: 'avatar',
        align:'center'
    },
    {
        title: '简介',
        dataIndex: 'profile',
        align:'center'
    },
    {
        title: '用户角色',
        dataIndex: 'role',
        align:'center'
    },
    {
        title: '创建时间',
        dataIndex: 'createTime',
        align:'center'
    },
    {
        title: '操作',
        key: 'action',
        align:'center'
    },
]

// 数据
const data = ref<API.UserVO[]>([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.UserQueryRequest>({
    pageNum: 1,
    pageSize: 10,
})

// 获取数据
const fetchData = async () => {
    const res = await listUserVoByPage({
        ...searchParams,
    })
    if (res.data.data) {
        data.value = res.data.data.records ?? []
        total.value = res.data.data.totalRow ?? 0
    } else {
        message.error('获取数据失败，' + res.data.message)
    }
}

// 分页参数
const pagination = computed(() => {
    return {
        current: searchParams.pageNum ?? 1,
        pageSize: searchParams.pageSize ?? 10,
        total: total.value,
        showSizeChanger: true,
        showTotal: (total: number) => `共 ${total} 条`,
    }
})

// 表格变化处理
const handleTableChange = (page: any) => {
    searchParams.pageNum = page.current
    searchParams.pageSize = page.pageSize
    fetchData()
}

// 获取数据
const handleSearch = () => {
    // 重置页码
    searchParams.pageNum = 1
    fetchData()
}


// 页面加载时请求一次
onMounted(() => {
    fetchData()
})

</script>



<style scoped>
#userManagePage{
    width: 90%;
}

</style>