<script setup lang="ts">
import { useChatService } from '@/composables/useChatService'
import { useAuthStore } from '@/stores/auth'
import { useChatStore } from '@/stores/chat'
import { useToast } from 'primevue/usetoast'
import { ref } from 'vue'
const message = ref('')
const loading = ref(false)

const chatService = useChatService()
const authStore = useAuthStore()
const chatStore = useChatStore()
const toast = useToast()

const handleSubmit = async () => {
  if (!message.value.trim()) return
  loading.value = true
  try {
    await chatService.sendMessage({
      text: message.value.trim(),
      userId: authStore.user?.id,
      convId: chatStore.currentConv?.id
    })
    if (!chatStore.currentChat) {
      throw Error('No current conversation!')
    }
    message.value = "";
  } catch (error: any) {
    toast.add({
      severity: 'error',
      detail: error?.message,
    })
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="h-full w-full">
    <form @submit.prevent="handleSubmit" class="flex">
      <input
        v-model="message"
        placeholder="Type a message..."
        class="w-full ps-8 bg-gray-900 rounded-none h-16"
        :disabled="loading"
      />
      <button
        type="submit"
        class="w-16 shrink-0 bg-lime-700 flex items-center justify-center cursor-pointer"
        :loading="loading"
        :disabled="!message.trim()"
      >
        <i v-if="loading" class="pi pi-spinner animate-spin"></i>
        <i v-else class="pi pi-send" style="font-size: 1.25rem"></i>
      </button>
    </form>
  </div>
</template>
