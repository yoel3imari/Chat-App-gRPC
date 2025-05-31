<script setup lang="ts">
import { onMounted, ref } from 'vue'
import Toast from 'primevue/toast'
import { useAuthStore } from './stores/auth'
import TokenService from './services/TokenService'
import { useAuthService } from './composables/useAuthService'
import { useRouter } from 'vue-router'
import SplashScreen from './components/SplashScreen.vue'

const authStore = useAuthStore()
const authService = useAuthService()
const router = useRouter()
const loading = ref(false)

onMounted(async () => {
  const token = TokenService.getAccessToken()
  const refresh = TokenService.getRefreshToken()

  if (token && refresh) {
    loading.value = true
    try {
      const res = await authService.refreshToken({ refreshToken: refresh })
      // console.log(res);

      if (!res.success) {
        throw Error(res.message)
      }

      TokenService.setTokens(res.accessToken, res.refreshToken)

      if (res.user) {
        authStore.setUser(res.user)
      }

      // redirect based on role

      if (res.user?.isAdmin) {
        await router.push({ name: 'dashboard' })
      } else {
        await router.push({ name: 'home' })
      }
    } catch (err) {
      console.error('Token refresh failed:', err)
      TokenService.clearTokens()
      authStore.setUser(null)
      await router.push({ name: 'login' })
    }
  } else {
    await router.push({ name: 'login' })
  }

  loading.value = false
})
</script>

<template>
  <Toast position="bottom-center" />
  <SplashScreen v-if="loading" />
  <RouterView v-else />
</template>
