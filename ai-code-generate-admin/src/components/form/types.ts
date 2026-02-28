import type { Rule } from 'ant-design-vue/es/form';

export interface FormSchema {
  // 字段名 (对应 model 中的 key)
  field: string;
  // 标签名
  label: string;
  // 组件类型 (支持内置类型 + slot)
  type: 'input' | 'password' | 'select' | 'date' | 'textarea' | 'slot';
  // 插槽名称 (当 type 为 slot 时必填)
  slotName?: string;
  // 传递给组件的 props (如 placeholder, options, disabled 等)
  componentProps?: Record<string, any>;
  // 校验规则
  rules?: Rule[];
  // 栅格布局属性 (例如 { span: 12 } 代表占一半宽度)
  colProps?: { span?: number; offset?: number; md?: number; sm?: number };
  // 默认值
  defaultValue?: any;
  // 是否隐藏 (支持动态判断)
  hidden?: boolean;
}