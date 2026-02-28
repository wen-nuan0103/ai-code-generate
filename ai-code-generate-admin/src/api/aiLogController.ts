// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 POST /ai/log/list/page */
export async function listAiGenerateLogByPage(
  body: API.AiLogQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageAiGenerateLogVO>('/ai/log/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
