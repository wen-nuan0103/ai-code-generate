// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 GET /health/ */
export async function health(options?: { [key: string]: any }) {
  return request<API.BaseResponseString>('/health/', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 PUT /health/ */
export async function health3(options?: { [key: string]: any }) {
  return request<API.BaseResponseString>('/health/', {
    method: 'PUT',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /health/ */
export async function health2(options?: { [key: string]: any }) {
  return request<API.BaseResponseString>('/health/', {
    method: 'POST',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 DELETE /health/ */
export async function health5(options?: { [key: string]: any }) {
  return request<API.BaseResponseString>('/health/', {
    method: 'DELETE',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 PATCH /health/ */
export async function health4(options?: { [key: string]: any }) {
  return request<API.BaseResponseString>('/health/', {
    method: 'PATCH',
    ...(options || {}),
  })
}
