<!-- src/components/ChatList.vue -->
<template>
  <div class="h-full flex flex-col">
    <div>
      <InputText v-model="search" placeholder="Search conversations..." class="w-full mb-4 h-10" />
    </div>

    <div v-if="loading" class="flex justify-center p-4">
      <i class="pi pi-spinner animate-spin"></i>
    </div>

    <div v-else-if="error" class="text-red-500 text-center p-4">
      {{ error }}
    </div>

    <div v-else-if="searchRes.length > 0">
      <ul class="flex flex-col gap-3">
        <li
          v-for="res in searchRes"
          :key="res.id.toString()"
          class="bg-gray-500/50 px-2 py-2 rounded"
        >
          <div class="flex items-center justify-center gap-2">
            <span class="shrink-0 w-8 h-8">
              <Avatar size="normal" shape="circle" image="/images/default_avatar.jpg" />
            </span>
            <span class="w-full">
              {{ res.username }}
            </span>

            <span
              class="text-lime-400 w-6 h-6 cursor-pointer flex items-center justify-center bg-lime-500/25 px-2 rounded"
              @click="handleSearchClick(res)"
            >
              <i class="pi pi-arrow-right"></i>
            </span>
          </div>
        </li>
      </ul>
    </div>

    <div v-else class="flex-1">
      <ul class="h-full w-full overflow-y-auto">
        <ChatItem
          v-for="[key, value] in Object.entries(chatStore.privateConv)"
          :key="key"
          :conversation="value"
        />
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useChatService } from '@/composables/useChatService'
import { useUserService } from '@/composables/useUserService'
import { useChatStore } from '@/stores/chat'
import { useAuthStore } from '@/stores/auth'
import type { User } from '@/grpc/user/user_pb'
import InputText from 'primevue/inputtext'
import ChatItem from './ChatItem.vue'
import { throttle } from '@/libs/utils'
import Avatar from 'primevue/avatar'
import { useRouter } from 'vue-router'
import { useChatStreams } from '@/composables/useChatStreams'

// Services and stores
const chatService = useChatService()
const userService = useUserService()
const chatStore = useChatStore()
const authStore = useAuthStore()
const router = useRouter()

// State
const search = ref('')
const searchRes = ref<User[]>([])
const loading = ref(false)
const error = ref<unknown>(null)

watch(
  search,
  throttle(async () => {
    loading.value = true
    try {
      // get users list by username or email
      if (!search.value) {
        searchRes.value = []
        return
      }
      const res = await userService.searchUsers({ searchTerm: search.value })
      console.log(res)
      searchRes.value = res.users
    } catch (error: any) {
      console.error(error)
    } finally {
      loading.value = false
    }
  }, 500),
)

async function handleSearchClick(user: User) {
  try {
    if (!authStore.user?.id) {
      throw Error('User unauthenticated')
    }
    // set current chat user
    chatStore.currentChat = user
    // create new conversation
    const res = await chatService.createPrivateConversation({
      currUserId: authStore.user.id,
      otherUserId: user.id,
    })

    const { streamManager } = useChatStreams()
    streamManager.startMessageStreamForConversation(res.id)

    // add conversation to store with otherUser.id as key
    chatStore.addPrivateConv(res)
    chatStore.setCurrentConv(res)
    // route to chat/:id (id is conversation id)
    router.push({ name: 'chat', params: { id: res.id.toString() } })
  } catch (error: any) {
    console.error(error)
  }
}
</script>

<style scoped>

</style>
