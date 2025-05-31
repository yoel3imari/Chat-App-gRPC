<!-- src/components/ChatWindow.vue -->
<template>
  <div class="flex-1 flex flex-col h-full">
    <!-- Header -->
    <ChatHeader v-if="chatStore.currentChat" :user="chatStore.currentChat" />

    <!-- Messages -->
    <div class="flex-1 p-4 overflow-y-auto space-y-4 flex justify-end" ref="messagesContainer">
      <div v-if="loading" class="flex justify-center items-center h-full">
        <i class="pi pi-spinner animate-spin"></i>
      </div>

      <div v-else-if="error" class="text-rose-500 text-center p-4">
        {{ error }}
      </div>

      <div
        v-else-if="currentMessages && currentMessages.length === 0"
        class="flex justify-center items-center h-full w-full text-gray-500"
      >
        No messages yet. Start the conversation!
      </div>
      <div v-else class="w-full flex flex-col justify-end">
        <div
          v-for="(message, index) in currentMessages"
          :key="index"
          class="flex flex-col items-end"
        >
          <!-- <div v-if="shouldShowDateSeparator(index)" class="flex justify-center my-4">
            <span class="text-sm text-gray-500 bg-gray-100 px-3 py-1 rounded-full">
              {{ formatDate(message.createdAt) }}
            </span>
          </div> -->

          <MessageBubble
            :text="message.text"
            :time="formatTime(message.createdAt)"
            :seen="message.status === MessageStatus.READ"
            :isOwn="message.userId === authStore.user?.id"
            :isEdited="message.edited"
            :status="getMessageStatus(message)"
            :username="
              message.userId === authStore.user?.id
                ? authStore.user?.username
                : chatStore.currentChat?.username
            "
          >
            {{ message.text }}
          </MessageBubble>
        </div>
      </div>
    </div>

    <!-- Input -->
    <div class="h-16">
      <ChatSender />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, useTemplateRef, watch } from 'vue'
import { useChatService } from '@/composables/useChatService'
import { useChatStore } from '@/stores/chat'
import { useAuthStore } from '@/stores/auth'
import ChatSender from './ChatSender.vue'
import ChatHeader from './ChatHeader.vue'
import { MessageStatus, type Message } from '@/grpc/chat/chat_pb'
import MessageBubble from './MessageBubble.vue'
import { useRoute } from 'vue-router'

const loading = ref(false)
const error = ref<unknown>(null)
const messagesContainer = useTemplateRef('messagesContainer')

const chatService = useChatService()
const chatStore = useChatStore()
const authStore = useAuthStore()

const route = useRoute()
const routeConvId = computed(() => {
  const id = route.params.id
  const stringId = Array.isArray(id) ? id[0] : (id ?? '0')
  return BigInt(stringId)
})
const currentConv = computed(() => chatStore.currentConv)
const currentMessages = computed(() => {
  const id = currentConv.value?.id
  return chatStore.messages[Number(id)] || []
})

onMounted(async () => {
  console.log('chat-window mounted')
  loading.value = true

  try {
    // get all messages by convId if visiting for first time
    //if (currentConv.value?.id && chatStore.convVisited.has(currentConv.value?.id)) return
    chatStore.resetUnreadCount()
    await getConvMessages()
  } catch (err) {
    console.log(err)
    error.value = err
  } finally {
    smoothScrollToBottom()
    loading.value = false
  }
})

async function getConvMessages() {
  const res = await chatService.getConvMessage({
    convId: currentConv.value?.id || routeConvId.value,
  })
  if (!res.success) throw Error(res.message)
  console.log('ConvMessage:', res)
  if (!currentConv.value?.id) throw Error('No current conversation')
  chatStore.addConvMessages(res.messageList, currentConv.value.id)
}

watch(
  [currentConv],
  async () => {
    console.log('watch triggered')
    await getConvMessages()
    smoothScrollToBottom()
  },
  { deep: true },
)

const formatTime = (timestamp: string) => {
  return new Date(timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

const getMessageStatus = (message: Message): string => {
  if (!authStore.user?.id || message.userId !== authStore.user?.id) return ''

  switch (message.status) {
    case MessageStatus.SENT:
      return 'sent'
    case MessageStatus.DELIVERED:
      return 'delivered'
    case MessageStatus.READ:
      return 'read'
    default:
      return ''
  }
}

function smoothScrollToBottom() {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTo({
      top: messagesContainer.value.scrollHeight,
      behavior: 'smooth',
    })
  }
}
</script>

<style scoped>
.messages-container {
  scroll-behavior: smooth;
}
</style>
