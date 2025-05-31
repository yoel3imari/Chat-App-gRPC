<template>
  <AuthLayout title="Login" subtitle="Access your account">
    <form @submit.prevent="handleLogin" class="space-y-6">
      <div>
        <label for="email" class="block text-sm font-medium text-gray-700 dark:text-gray-300"
          >Email address</label
        >
        <InputText
          id="email"
          type="email"
          v-model="email"
          class="mt-1 block w-full"
          required
          placeholder="you@example.com"
        />
      </div>

      <div>
        <label for="password" class="block text-sm font-medium text-gray-700 dark:text-gray-300"
          >Password</label
        >
        <Password
          id="password"
          v-model="password"
          class="mt-1 block w-full"
          required
          placeholder="Your Password"
          :feedback="false"
          toggleMask
          inputClass="w-full"
        />
      </div>

      <Button
        type="submit"
        label="Sign In"
        class="w-full !bg-indigo-600 hover:!bg-indigo-700 text-white"
        :loading="isLoading"
      />
    </form>

    <template #footer>
      <router-link
        to="/register"
        class="font-medium text-indigo-600 hover:text-indigo-500 dark:text-indigo-400 dark:hover:text-indigo-300"
      >
        Don't have an account? Sign Up
      </router-link>
      <br />
      <router-link
        to="/forgot-password"
        class="mt-2 text-sm text-gray-500 hover:text-gray-600 dark:text-gray-400 dark:hover:text-gray-300"
      >
        Forgot password?
      </router-link>
    </template>
  </AuthLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router' // Assuming you use vue-router
import AuthLayout from '@/layouts/AuthLayout.vue' // Adjust path if needed

// Import PrimeVue components used in the form
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Button from 'primevue/button'
import { useAuthService } from '@/composables/useAuthService'
import { useAuthStore } from '@/stores/auth'
import TokenService from '@/services/TokenService'
import { useToast } from 'primevue/usetoast'
// import Message from 'primevue/message'; // For error display

const router = useRouter() // If using router
const authService = useAuthService()
const authStore = useAuthStore()
const toast = useToast()

const email = ref('youssef@gmail.com')
const password = ref('password')
const isLoading = ref(false)
// const emailError = ref(''); // Example validation state

const handleLogin = async () => {
  isLoading.value = true
  // emailError.value = ''; // Reset errors
  console.log('Logging in with:', email.value)

  try {
    const res = await authService.login({ email: email.value, password: password.value })

    if (!res.success) {
      throw Error(res.message)
    }

    TokenService.setTokens(res.accessToken, res.refreshToken)

    if (res.user) {
      authStore.setUser(res.user)
    }

    toast.add({
      severity: 'success',
      summary: 'Login',
      detail: 'You are logged in!',
      life: 2000,
    })

    if (res.user?.isAdmin) {
      await router.push({ name: 'dashboard' })
    } else {
      await router.push({ name: 'home' })
    }

  } catch (error: any) {
    toast.add({
      severity: 'error',
      summary: 'Login failed',
      detail: error,
    })

    console.log(error)
  }

  isLoading.value = false

}
</script>

<style>
/* Ensure password inputs fill width */
.p-password input {
  width: 100%;
}
</style>
