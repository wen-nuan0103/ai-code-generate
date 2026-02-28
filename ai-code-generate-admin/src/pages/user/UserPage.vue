<template>
    <div class="page-wrapper">
        <BasicTable title="用户管理" search-text="请输入用户账号" :columns="columns" :data-source="data" :loading="loading"
            :pagination="searchParams" @change="handleTableChange" @refresh="fetchData">

            <template #searchExtend="{ model }">

                <!-- <span style="margin-left: 12px">状态：</span> -->
                <a-select size="large" v-model:value="model.userStatus" placeholder="请选择状态" style="width: 120px"
                    allow-clear>
                    <a-select-option :value="0">启用</a-select-option>
                    <a-select-option :value="1">禁用</a-select-option>
                </a-select>

                <!-- <span style="margin-left: 12px">角色：</span> -->
                <a-select size="large" v-model:value="model.role" placeholder="全部角色" style="width: 120px" allow-clear>
                    <a-select-option value="admin">管理员</a-select-option>
                    <a-select-option value="user">普通用户</a-select-option>
                </a-select>

            </template>

            <template #toolbar>
                <a-button type="primary" @click="handleAdd">
                    <template #icon>
                        <PlusOutlined />
                    </template>
                    新增用户
                </a-button>
                <a-button>导出</a-button>
            </template>

            <template #avatar="{ text }">
                <a-avatar :src="text" />
            </template>

            <template #userStatus="{ text }">
                <a-tag :color="text === 0 ? 'green' : 'red'">
                    {{ text === 0 ? '启用' : '禁用' }}
                </a-tag>
            </template>

            <template #action="{ record }">
                <a @click="handleEdit(record)">编辑</a>
                <a-divider type="vertical" />
                <a-popconfirm title="确定删除吗？" @confirm="handleDelete(record.id)">
                    <a style="color: red">删除</a>
                </a-popconfirm>
            </template>

        </BasicTable>

        <a-modal v-model:open="modalVisible" title="编辑用户" @ok="handleModalOk" :confirmLoading="confirmLoading">
            <a-form ref="formRef" :model="formState" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
                <a-form-item label="用户账号">
                    <a-input v-model:value="formState.userAccount" disabled size="large"/>
                </a-form-item>

                <!-- <a-form-item label="用户头像">
                    <div style="display: flex; align-items: center; gap: 10px;">
                        <a-avatar :src="formState.avatar" />
                        <a-input v-model:value="formState.avatar" placeholder="请输入头像URL" />
                    </div>
                </a-form-item> -->

                <a-form-item label="头像" class="avatar-item">
                    <a-upload  name="file" list-type="picture-card" class="avatar-uploader" :show-upload-list="false"
                        action="/api/file/upload" :with-credentials="true" @change="handleUploadChange">
                        <img v-if="formState.avatar" :src="formState.avatar" alt="avatar" class="avatar-img" />
                        <div v-else>
                            <loading-outlined v-if="uploadLoading" />
                            <plus-outlined v-else />
                            <div class="ant-upload-text">上传</div>
                        </div>
                    </a-upload>
                    <div class="upload-tip">建议尺寸 200x200，支持 jpg/png，最大 2MB</div>
                </a-form-item>

                <a-form-item label="用户昵称" name="userName" :rules="[{ required: true, message: '请输入昵称' }]">
                    <a-input v-model:value="formState.userName" placeholder="请输入昵称" size="large"/>
                </a-form-item>

                <a-form-item label="用户角色" name="userRole" >
                    <a-select v-model:value="formState.role" size="large">
                        <a-select-option value="user">普通用户</a-select-option>
                        <a-select-option value="admin">管理员</a-select-option>
                    </a-select>
                </a-form-item>

                <a-form-item label="账号状态" name="userStatus" >
                    <a-radio-group v-model:value="formState.userStatus" size="large">
                        <a-radio :value="0">正常</a-radio>
                        <a-radio :value="1">禁用</a-radio>
                    </a-radio-group>
                </a-form-item>
            </a-form>
        </a-modal>

    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { PlusOutlined,LoadingOutlined } from '@ant-design/icons-vue';

import BasicTable from '@/components/table/BaseTable.vue';
import type { TableColumn } from '@/components/table/types';
import { listUserVoByPage, updateUser } from '@/api/userController';
import { message, type UploadChangeParam } from 'ant-design-vue';

// 定义列
const columns: TableColumn[] = [
    //   { title: 'ID', dataIndex: 'id', width: 80 },
    { title: '头像', dataIndex: 'avatar', width: 100 },
    { title: '用户名', dataIndex: 'userName' },
    { title: '账号', dataIndex: 'userAccount' },
    { title: '角色', dataIndex: 'role' },
    { title: '状态', dataIndex: 'userStatus' }, // 将会被插槽自定义
    { title: '创建时间', dataIndex: 'createTime' }, // 将会被插槽自定义
    { title: '操作', dataIndex: 'action', width: 150, fixed: 'right' }, // 操作列
];

// 用户数据
const data = ref<API.UserVO[]>([])
const total = ref(0)

// 编辑用户
const modalVisible = ref(false);
const confirmLoading = ref(false);
const formRef = ref();

// 状态管理
const loading = ref(false);
const initSearchParams = {
    current: 1,
    pageSize: 10,
    total: 0,
    userAccount: undefined,
    role: undefined,
    userStatus: undefined
};
const searchParams = reactive({
    ...initSearchParams
})
const uploadLoading = ref(false)
const initFormState = reactive({
    id: undefined,
    userAccount: '',
    userName: '',
    avatar: '',
    role: undefined,
    userStatus: undefined,
});
const formState = reactive({
    ...initFormState
});

// 数据请求
const fetchData = async () => {
    loading.value = true;

    const paramsToSend: Record<string, any> = {};

    // 遍历 searchParams
    for (const key in searchParams) {
        const value = (searchParams as any)[key];
        if (value !== null && value !== undefined && value !== '') {
            paramsToSend[key] = value;
        }
    }
    const res = await listUserVoByPage(paramsToSend);
    try {
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

// 事件处理
const handleTableChange = (pag: any) => {
    console.log(pag);

    // ES6 的 解构赋值 与 剩余属性 
    const { keyword, ...restParams } = pag;
    Object.assign(searchParams, initSearchParams, {
        ...restParams,
        userAccount: keyword
    });

    fetchData();
};

const handleAdd = () => { console.log('点击新增'); };
const handleEdit = (record: any) => {
    modalVisible.value = true;
    Object.assign(formState, {
        id: record.id,
        userAccount: record.userAccount,
        userName: record.userName,
        avatar: record.avatar,
        role: record.role,
        userStatus: record.userStatus,
    });
};
// 处理头像上传
const handleUploadChange = (info: UploadChangeParam) => {
    if (info.file.status === 'uploading') {
        uploadLoading.value = true
        return
    }

    if (info.file.status === 'done') {
        const response = info.file.response

        if (response.code === 0) {
            const fileUrl = response.data
            formState.avatar = fileUrl

            uploadLoading.value = false
            message.success('头像上传成功')
        } else {
            uploadLoading.value = false
            message.error('上传失败：' + response.message)
        }
    }

    if (info.file.status === 'error') {
        uploadLoading.value = false
        message.error('网络错误，头像上传失败')
    }
}
// 点击弹窗“确定”
const handleModalOk = async () => {
    try {
        await formRef.value.validate();
        confirmLoading.value = true;

        const res = await updateUser(formState);

        if (res.data.code === 0) {
            message.success('修改成功');
            modalVisible.value = false;
            fetchData();
        } else {
            message.error('修改失败：' + res.data.message);
        }
    } catch (error) {
        console.error('校验失败或请求异常', error);
    } finally {
        confirmLoading.value = false;
    }
};
const handleDelete = (id: number) => { console.log('删除 ID:', id); };

onMounted(() => {
    fetchData();
});
</script>
<style scoped>

/* 上传头像样式优化 */
.avatar-uploader :deep(.ant-upload) {
    width: 100px;
    height: 100px;
    /* border-radius: 12%; */
    overflow: hidden;
}

.avatar-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.upload-tip {
    font-size: 12px;
    color: #94a3b8;
    margin-top: 8px;
}

</style>