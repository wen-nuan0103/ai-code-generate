<template>
  <a-form
    ref="formRef"
    :model="modelValue"
    :layout="layout"
    :label-col="labelCol"
    :wrapper-col="wrapperCol"
  >
    <a-row :gutter="24">
      <template v-for="schema in schemas" :key="schema.field">
        <a-col v-show="!schema.hidden" v-bind="getColProps(schema)">
          <a-form-item
            :label="schema.label"
            :name="schema.field"
            :rules="schema.rules"
          >
            <template v-if="schema.type === 'slot'">
              <slot
                :name="schema.slotName"
                :model="modelValue"
                :field="schema.field"
              ></slot>
            </template>

            <component
              v-else
              :is="componentMap[schema.type]"
              v-model:value="modelValue[schema.field]"
              v-bind="resolveComponentProps(schema)"
              style="width: 100%"
            />
          </a-form-item>
        </a-col>
      </template>

      <a-col :span="24" v-if="showActionButtonGroup">
        <a-form-item :wrapper-col="{ span: 24, offset: 0 }">
           <slot name="action">
             <a-space>
                <a-button type="primary" @click="submit" :loading="loading">提交</a-button>
                <a-button @click="reset">重置</a-button>
             </a-space>
           </slot>
        </a-form-item>
      </a-col>
    </a-row>
  </a-form>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import type { FormInstance } from 'ant-design-vue';
import type { FormSchema } from './types';
import {
  Input,
  Select,
  DatePicker,
  InputPassword,
  Textarea,
} from 'ant-design-vue';

// Props 定义
interface Props {
  schemas: FormSchema[];
  modelValue: Record<string, any>; // 双向绑定的表单数据
  layout?: 'horizontal' | 'vertical' | 'inline';
  labelCol?: object;
  wrapperCol?: object;
  showActionButtonGroup?: boolean; // 是否显示底部按钮
  loading?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  layout: 'vertical', // 默认垂直布局
  showActionButtonGroup: true,
  loading: false,
});

const emit = defineEmits(['update:modelValue', 'submit', 'reset']);

// 组件映射表 (可以将字符串映射为真实组件)
const componentMap: Record<string, any> = {
  input: Input,
  password: InputPassword,
  textarea: Input.TextArea,
  select: Select,
  date: DatePicker,
};

// 表单实例
const formRef = ref<FormInstance>();

// 工具方法
const getColProps = (schema: FormSchema) => {
  return schema.colProps || { span: 24 }; // 默认占满一行
};

// 处理组件 Props (例如 select 需要 options)
const resolveComponentProps = (schema: FormSchema) => {
  return {
    placeholder: `请输入${schema.label}`,
    allowClear: true,
    ...schema.componentProps,
  };
};

// 暴露给父组件的方法
const submit = async () => {
  try {
    await formRef.value?.validate();
    emit('submit', props.modelValue);
  } catch (error) {
    console.error('表单校验失败', error);
  }
};

const reset = () => {
  formRef.value?.resetFields();
  emit('reset');
};

// 暴露方法给父组件 ref 调用
defineExpose({
  submit,
  reset,
  formRef
});
</script>