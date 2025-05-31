import { useAuthService } from '@/composables/useAuthService'
import type { User } from '@/grpc/user/user_pb'
import TokenService from '@/services/TokenService'
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAppStore } from './app'
import { useChatStore } from './chat'

export const useAuthStore = defineStore('auth', () => {
  const router = useRouter()
  const authStore = useAuthStore()
  const authService = useAuthService()
  const appStore = useAppStore()

  const user = ref<User | null>({} as User)

  const setUser = (data: User | null) => {
    user.value = data
  }

  function getUserId() {
    if (!user.value?.id) {
      logout()
      return
    }

    return Number(user.value.id)
  }

  const purge = () => {
    user.value = null
  }

  const logout = async () => {
    try {
      await authService.logout({ userId: authStore.user?.id })
      const chatStore = useChatStore()
      chatStore.resetChatState()
    } catch (error: any) {
      console.error(error)
    } finally {
      authStore.purge()
      TokenService.clearTokens()
      appStore.setSettingsModalVisible(false)
      await router.push({ name: 'login' })
    }
  }

  return {
    user,
    getUserId,
    setUser,
    purge,
    logout,
  }
})
