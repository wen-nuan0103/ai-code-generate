import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getLoginUser } from '@/api/userController.ts'

export const useUserStore = defineStore('user', () => {
  const user = ref<API.LoginUserVO>({
    userName: '未登录',
  })

  // 获取登录用户信息
  async function fetchLoginUser() {
    const res = await getLoginUser()
    if (res.data.code === 0 && res.data.data) {
      user.value = res.data.data
    }
  }
  // 更新登录用户信息
  function setUser(newUser: any) {
    user.value = newUser
  }

  return { user, setUser, fetchLoginUser }
})
