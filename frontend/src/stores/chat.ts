import type { GroupConv, Message, PrivateConv } from '@/grpc/chat/chat_pb'
import type { User } from '@/grpc/user/user_pb'
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useAuthStore } from './auth'

export const useChatStore = defineStore('chat', () => {
  const currentChat = ref<User | null>(null)
  const currentConv = ref<PrivateConv | null>(null)
  const convVisited = ref<Set<bigint>>(new Set())
  // this is for sidebar list
  const privateConv = ref<Record<string, PrivateConv>>({})
  const groupConv = ref<Record<number, GroupConv[]>>({})

  function addConvToVisited(conv: PrivateConv) {
    if (convVisited.value.has(conv.id)) return
    convVisited.value.add(conv.id)
  }

  function resetUnreadCount() {
    if (currentConv.value) {
      currentConv.value.unreadCount = 0
    }
  }

  // conversations data
  const messages = ref<Record<number, Message[]>>({})

  // Stream management
  const activeStreams = ref({
    conversationStream: null as any,
    messageStreams: new Map<number, any>(), // convId -> stream reference
    streamControllers: new Map<number, AbortController>(), // convId -> AbortController
  })

  function addConvMessages(msgList: Message[], convId: bigint) {
    const id = Number(convId)
    const newObj: Record<number, Message[]> = {}
    newObj[id] = msgList
    messages.value = {
      ...messages.value,
      ...newObj,
    }
  }

  function pushMessage(msg: Message, convId: bigint) {
    const id = Number(convId)
    messages.value[id].push(msg)
    privateConv.value[convId.toString()].unreadCount += 1
    privateConv.value[convId.toString()].lastMessage = msg.text
  }

  function setPrivConc(list: PrivateConv[]) {
    const newObj: Record<string, PrivateConv> = {}
    list.forEach((conv) => {
      newObj[conv.id.toString()] = conv
    })
    privateConv.value = newObj // Replace whole object — triggers reactivity
  }

  function addPrivateConv(newConv: PrivateConv) {
    privateConv.value = {
      [newConv.id.toString()]: newConv,
      ...privateConv.value,
    }
  }

  function updatePrivConv(conv: PrivateConv) {
    privateConv.value[conv.id.toString()] = conv
  }

  function getOtherUser(conv: PrivateConv) {
    const { getUserId } = useAuthStore()
    return Number(conv.user2?.id) === getUserId() ? conv.user1 : conv.user2
  }

  function setCurrentConv(conv: PrivateConv) {
    if (conv && conv.user1 && conv.user2) {
      const { getUserId } = useAuthStore()
      currentChat.value = Number(conv.user2.id) === getUserId() ? conv.user1 : conv.user2
      currentConv.value = conv
      addConvToVisited(conv)
    }
  }

  // Stream management methods
  function setConversationStream(stream: any) {
    activeStreams.value.conversationStream = stream
  }

  function addMessageStream(convId: bigint, stream: any, controller: AbortController) {
    const id = Number(convId)
    activeStreams.value.messageStreams.set(id, stream)
    activeStreams.value.streamControllers.set(id, controller)
  }

  function removeMessageStream(convId: bigint) {
    const id = Number(convId)
    const controller = activeStreams.value.streamControllers.get(id)
    if (controller) {
      controller.abort()
    }
    activeStreams.value.messageStreams.delete(id)
    activeStreams.value.streamControllers.delete(id)
  }

  // Cleanup all streams
  function cleanupAllStreams() {
    console.log('Cleaning up all chat streams...')

    // Close conversation stream
    if (activeStreams.value.conversationStream) {
      try {
        activeStreams.value.conversationStream.cancel?.()
      } catch (error: any) {
        console.warn('Error closing conversation stream:', error)
      }
      activeStreams.value.conversationStream = null
    }

    // Close all message streams
    for (const [convId, controller] of activeStreams.value.streamControllers) {
      try {
        controller.abort()
      } catch (error: any) {
        console.warn(`Error closing message stream for conversation ${convId}:`, error)
      }
    }

    activeStreams.value.messageStreams.clear()
    activeStreams.value.streamControllers.clear()
  }

  // Reset store state (for logout)
  function resetChatState() {
    cleanupAllStreams()
    currentChat.value = null
    currentConv.value = null
    privateConv.value = {}
    groupConv.value = {}
    messages.value = {}
  }

  return {
    currentChat,
    currentConv,
    privateConv,
    groupConv,
    messages,
    activeStreams,
    convVisited,

    setPrivConc,
    updatePrivConv,
    setCurrentConv,
    addPrivateConv,
    addConvMessages,
    pushMessage,
    resetUnreadCount,
    getOtherUser,

    // Stream management
    setConversationStream,
    addMessageStream,
    removeMessageStream,
    cleanupAllStreams,
    resetChatState,
  }
})
