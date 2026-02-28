import router from '@/router'
import { useUserStore } from '@/stores/user'
import ACCESS_ENUM from './accessEnum'
import checkAccess from './checkAccess'



router.beforeEach(async (to, from, next) => {
    const userStore = useUserStore()
    let loginUser = userStore.user
    // 如果之前没登陆过，自动登录
    if (!loginUser || !loginUser.role) {
        // 加 await 是为了等用户登录成功之后，再执行后续的代码
        await userStore.fetchLoginUser();
        loginUser = userStore.user;
    }

    console.log('登陆用户信息', loginUser)
    const needAccess = (to.meta?.access as string) ?? ACCESS_ENUM.NOT_LOGIN
    // 要跳转的页面必须要登陆
    if (needAccess !== ACCESS_ENUM.NOT_LOGIN) {
        // 如果没登陆，跳转到登录页面
        if (!loginUser || !loginUser.role || loginUser.role === ACCESS_ENUM.NOT_LOGIN) {
            next(`/login?redirect=${to.fullPath}`)
            return
        }
        // 如果已经登陆了，但是权限不足，那么跳转到无权限页面
        if (!checkAccess(loginUser, needAccess)) {
            next('/noAuth')
            return
        }
    }
    next()
})
