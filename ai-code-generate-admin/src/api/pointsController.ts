// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 POST /points/admin/charge */
export async function chargePoints(body: API.AdminChargeRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseLong>('/points/admin/charge', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /points/admin/list */
export async function listPointsLogVoByPage(
  body: API.PointsQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePagePointsVO>('/points/admin/list', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
