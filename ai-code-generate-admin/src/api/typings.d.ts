declare namespace API {
  type AdminChargeRequest = {
    userId?: number
    amount?: number
    remark?: string
  }

  type AiGenerateLogVO = {
    id?: number
    traceId?: string
    userId?: number
    taskType?: string
    modelName?: string
    inputTokens?: number
    outputTokens?: number
    totalCost?: number
    status?: number
    errorMsg?: string
    duration?: number
    createTime?: string
  }

  type AiKeyAddRequest = {
    provider?: string
    accessKey?: string
  }

  type AiKeyQueryRequest = {
    current?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    provider?: string
    key?: string
  }

  type AiKeyVO = {
    id?: string
    provider?: string
    accessKey?: string
    balance?: number
    status?: number
    lastUsedTime?: string
    errorCount?: number
  }

  type AiLogQueryRequest = {
    current?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    traceId?: string
    status?: number
  }

  type AiModel = {
    id?: number
    modelName?: string
    modelCode?: string
    modelType?: string
    provider?: string
    baseUrl?: string
    maxTokens?: number
    inputPrice?: number
    outputPrice?: number
    status?: number
    createTime?: string
    updateTime?: string
  }

  type AiModelAddRequest = {
    modelName?: string
    modelCode?: string
    modelType?: string
    provider?: string
    baseUrl?: string
    maxTokens?: number
  }

  type AiModelQueryRequest = {
    current?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    modelName?: string
    modelType?: string
  }

  type AiModelUpdateRequest = {
    id?: string
    modelName?: string
    modelCode?: string
    modelType?: string
    provider?: string
    baseUrl?: string
    maxTokens?: number
    status?: number
  }

  type AiModelVO = {
    id?: string
    modelName?: string
    modelCode?: string
    modelType?: string
    provider?: string
  }

  type AppAddRequest = {
    initPrompt?: string
  }

  type AppAdminUpdateRequest = {
    id?: number
    appName?: string
    cover?: string
    priority?: number
  }

  type AppDeployRequest = {
    appId?: number
  }

  type AppQueryRequest = {
    current?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    appName?: string
    cover?: string
    initPrompt?: string
    codeGeneratorType?: string
    tags?: string
    deployKey?: string
    deployStatus?: number
    priority?: number
    userId?: number
    currentStatus?: number
    version?: number
  }

  type AppUpdateRequest = {
    id?: number
    appName?: string
  }

  type AppVO = {
    id?: number
    appName?: string
    cover?: string
    initPrompt?: string
    codeGeneratorType?: string
    tags?: string[]
    deployKey?: string
    deployStatus?: number
    deployedTime?: string
    priority?: number
    userId?: number
    scopeStatus?: number
    chatScopeStatus?: number
    currentStatus?: number
    version?: number
    createTime?: string
    updateTime?: string
    user?: UserVO
  }

  type BaseResponseAiModel = {
    code?: number
    data?: AiModel
    message?: string
  }

  type BaseResponseAppVO = {
    code?: number
    data?: AppVO
    message?: string
  }

  type BaseResponseBoolean = {
    code?: number
    data?: boolean
    message?: string
  }

  type BaseResponseListAiModelVO = {
    code?: number
    data?: AiModelVO[]
    message?: string
  }

  type BaseResponseLoginUserVO = {
    code?: number
    data?: LoginUserVO
    message?: string
  }

  type BaseResponseLong = {
    code?: number
    data?: number
    message?: string
  }

  type BaseResponsePageAiGenerateLogVO = {
    code?: number
    data?: PageAiGenerateLogVO
    message?: string
  }

  type BaseResponsePageAiKeyVO = {
    code?: number
    data?: PageAiKeyVO
    message?: string
  }

  type BaseResponsePageAiModel = {
    code?: number
    data?: PageAiModel
    message?: string
  }

  type BaseResponsePageAppVO = {
    code?: number
    data?: PageAppVO
    message?: string
  }

  type BaseResponsePageChatHistory = {
    code?: number
    data?: PageChatHistory
    message?: string
  }

  type BaseResponsePagePointsVO = {
    code?: number
    data?: PagePointsVO
    message?: string
  }

  type BaseResponsePageUserVO = {
    code?: number
    data?: PageUserVO
    message?: string
  }

  type BaseResponseString = {
    code?: number
    data?: string
    message?: string
  }

  type BaseResponseUser = {
    code?: number
    data?: User
    message?: string
  }

  type BaseResponseUserVO = {
    code?: number
    data?: UserVO
    message?: string
  }

  type BaseResponseVoid = {
    code?: number
    data?: Record<string, any>
    message?: string
  }

  type ChatHistory = {
    id?: number
    thinkingContent?: string
    message?: string
    messageType?: string
    parentId?: number
    appId?: number
    userId?: number
    createTime?: string
    updateTime?: string
    isDelete?: number
  }

  type ChatHistoryQueryRequest = {
    current?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    message?: string
    messageType?: string
    appId?: number
    userId?: number
    lastCreateTime?: string
  }

  type chatParams = {
    appId: number
    userMessage: string
  }

  type DeleteRequest = {
    id?: number
  }

  type downloadAppCodeParams = {
    appId: number
  }

  type exportMarkdownParams = {
    appId: number
  }

  type getAiModelByIdParams = {
    id: number
  }

  type getAppVOByIdAdminParams = {
    id: number
  }

  type getAppVOByIdParams = {
    id: number
  }

  type getUserByIdParams = {
    id: number
  }

  type getUserVOByIdParams = {
    id: number
  }

  type listAiModelVOParams = {
    modelName: string
  }

  type listAppChatHistoryParams = {
    appId: number
    pageNum?: number
    lastCreateTime?: string
  }

  type LoginUserVO = {
    id?: number
    userAccount?: string
    phone?: string
    userName?: string
    avatar?: string
    profile?: string
    role?: string
    vipExpireTime?: string
    vipCode?: string
    vipNumber?: number
    points?: number
    shareCode?: string
    inviteUser?: number
    createTime?: string
    updateTime?: string
  }

  type PageAiGenerateLogVO = {
    records?: AiGenerateLogVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageAiKeyVO = {
    records?: AiKeyVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageAiModel = {
    records?: AiModel[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageAppVO = {
    records?: AppVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageChatHistory = {
    records?: ChatHistory[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PagePointsVO = {
    records?: PointsVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageUserVO = {
    records?: UserVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PointsQueryRequest = {
    current?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    userAccount?: string
    bizNo?: string
    type?: number
    createTimeStart?: string
    createTimeEnd?: string
  }

  type PointsVO = {
    id?: number
    userId?: number
    amount?: number
    type?: number
    currentPoints?: number
    bizNo?: string
    refId?: number
    remark?: string
    createTime?: string
    isIncome?: boolean
    userAccount?: string
    userName?: string
    avatar?: string
  }

  type ServerSentEventString = true

  type serveStaticResourceParams = {
    deployKey: string
  }

  type User = {
    id?: number
    userAccount?: string
    phone?: string
    password?: string
    userName?: string
    avatar?: string
    profile?: string
    role?: string
    vipExpireTime?: string
    vipCode?: string
    vipNumber?: number
    points?: number
    shareCode?: string
    inviteUser?: number
    editTime?: string
    createTime?: string
    updateTime?: string
    userStatus?: number
    isDelete?: number
  }

  type UserAddRequest = {
    userName?: string
    userAccount?: string
    avatar?: string
    profile?: string
    role?: string
  }

  type UserLoginRequest = {
    userAccount?: string
    userPassword?: string
    isAdminLogin?: boolean
  }

  type UserQueryRequest = {
    current?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    userName?: string
    userAccount?: string
    userProfile?: string
    role?: string
    userStatus?: number
  }

  type UserRegisterRequest = {
    userAccount?: string
    userPassword?: string
    checkPassword?: string
    inviteCode?: string
  }

  type UserUpdateByAdminRequest = {
    id?: number
    userName?: string
    avatar?: string
    profile?: string
    role?: string
    userStatus?: number
  }

  type UserUpdateRequest = {
    id?: number
    userName?: string
    avatar?: string
    profile?: string
  }

  type UserVO = {
    id?: number
    userAccount?: string
    userName?: string
    avatar?: string
    profile?: string
    role?: string
    isVip?: boolean
    userStatus?: number
    createTime?: string
  }
}
