/**
 * 代码部署类型枚举
 */
export enum CodeDeployTypeEnum {
    UN_DEPLOY = 0,
    DEPLOYED = 1,
    DEPLOY_FAIL = 2
}

/**
 * 代码部署类型配置
 */
export const CODE_DEPLOY_TYPE_CONFIG = {
    [CodeDeployTypeEnum.UN_DEPLOY]: {
        label: '未部署',
        value: CodeDeployTypeEnum.UN_DEPLOY,
    },
    [CodeDeployTypeEnum.DEPLOYED]: {
        label: '已部署',
        value: CodeDeployTypeEnum.DEPLOYED,
    },
    [CodeDeployTypeEnum.DEPLOY_FAIL]: {
        label: '部署失败',
        value: CodeDeployTypeEnum.DEPLOY_FAIL,
    },
} as const

/**
 * 代码部署类型选项（用于下拉选择）
 */
export const CODE_DEPLOY_TYPE_OPTIONS = Object.values(CODE_DEPLOY_TYPE_CONFIG).map((config) => ({
    label: config.label,
    value: config.value,
}))

/**
 * 格式化代码部署类型
 * @param type 代码部署类型
 * @returns 格式化后的类型描述
 */
export const formatCodeDeployType = (type: number | undefined): string => {
    if (!type) return '未知类型'

    const config = CODE_DEPLOY_TYPE_CONFIG[type as CodeDeployTypeEnum]
    return config ? config.label : type + ""
}

/**
 * 获取所有代码部署类型
 */
export const getAllCodeGenTypes = () => {
    return Object.values(CodeDeployTypeEnum)
}

/**
 * 检查是否为有效的代码部署类型
 * @param type 待检查的类型
 */
export const isValidCodeGenType = (type: number): type is CodeDeployTypeEnum => {
    return Object.values(CodeDeployTypeEnum).includes(type as CodeDeployTypeEnum)
}
