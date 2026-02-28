// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 POST /ai/key/add */
export async function addAiApiKey(body: API.AiKeyAddRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseVoid>('/ai/key/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /ai/key/list/page */
export async function listAiApiKeyByPage(
  body: API.AiKeyQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageAiKeyVO>('/ai/key/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
