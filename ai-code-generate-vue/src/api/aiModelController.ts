// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 POST /ai/model/add */
export async function addAiModel(body: API.AiModelAddRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseVoid>('/ai/model/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /ai/model/delete */
export async function deleteAiModel(body: API.DeleteRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/ai/model/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /ai/model/get */
export async function getAiModelById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getAiModelByIdParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseAiModel>('/ai/model/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /ai/model/list/page */
export async function listAiModelByPage(
  body: API.AiModelQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageAiModel>('/ai/model/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /ai/model/list/vo */
export async function listAiModelVo(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.listAiModelVOParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseListAiModelVO>('/ai/model/list/vo', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /ai/model/update */
export async function updateAiModel(
  body: API.AiModelUpdateRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/ai/model/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
