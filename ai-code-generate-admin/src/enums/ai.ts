// 定义供应商的类型
export type AiProvider = 'OpenAI' | 'DeepSeek' | 'GLM' | 'Claude' | 'Google' | 'Aliyun' | 'Moonshot';

// 定义选项列表
export const AI_PROVIDER_OPTIONS: { label: string; value: AiProvider; color?: string }[] = [
  { label: 'OpenAI (GPT)', value: 'OpenAI', color: 'green' },
  { label: 'DeepSeek (深度求索)', value: 'DeepSeek', color: 'blue' },
  { label: '智谱 (GLM)', value: 'GLM', color: 'orange' },
  { label: 'Anthropic (Claude)', value: 'Claude', color: 'purple' },
  { label: 'Google (Gemini)', value: 'Google', color: 'red' },
  { label: '阿里云 (通义千问)', value: 'Aliyun', color: 'cyan' },
];

// 如果需要键值对映射
export const AI_PROVIDER_MAP = AI_PROVIDER_OPTIONS.reduce((acc, cur) => {
  acc[cur.value] = cur.label;
  return acc;
}, {} as Record<string, string>);