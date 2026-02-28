/**
 * 环境变量配置
 */

const env = import.meta.env;

// 应用部署域名
export const DEPLOY_DOMAIN = env.VITE_DEPLOY_DOMAIN || 'http://localhost'

// API 基础地址
export const API_BASE_URL = env.VITE_API_BASE_URL || 'http://localhost:8123/api'

// 静态资源地址
export const STATIC_BASE_URL = `${API_BASE_URL}/static`

// 获取部署应用的完整URL
export const getDeployUrl = (deployKey: string) => {
  return `${DEPLOY_DOMAIN}/${deployKey}`
}

console.log('当前环境配置:', {
  API_BASE_URL,
  MODE: env.MODE
});