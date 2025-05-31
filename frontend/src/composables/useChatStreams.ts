import { useChatStore } from '@/stores/chat'
import { useAuthStore } from '@/stores/auth' // Adjust import path
import { onUnmounted } from 'vue'
import { useChatService } from './useChatService'
import type { PrivateConv } from '@/grpc/chat/chat_pb'

class ChatStreamManager {
  constructor(
    private chatService: ReturnType<typeof useChatService>, //
    private chatStore: ReturnType<typeof useChatStore>,
    private authStore: ReturnType<typeof useAuthStore>,
  ) {}

  async initializeChatStreams() {
    try {
      // Get initial conversations
      const chatRes = await this.chatService.getPrivateConversations({
        userId: this.authStore.user?.id,
      })
      console.log('initializeChatStreams: ', chatRes)

      if (!chatRes.success) {
        throw new Error(chatRes.message)
      }

      console.log('Initial conversations loaded:', chatRes)
      this.chatStore.setPrivConc(chatRes.privateConvList)
      chatRes.privateConvList.forEach((c) => this.chatStore.addConvMessages([], c.id))
      // Start conversation stream
      await this.startConversationStream()

      // Start message streams for existing conversations
      await this.startMessageStreams(chatRes.privateConvList)
    } catch (error: any) {
      console.error('Failed to initialize chat streams:', error)
      throw error
    }
  }

  async startConversationStream() {
    try {
      const convStream = this.chatService.streamPrivateConversations({
        userId: this.authStore.user?.id,
      })

      this.chatStore.setConversationStream(convStream)

      // Handle conversation stream in background
      ;(async () => {
        try {
          for await (const conv of convStream) {
            console.log('New/updated conversation:', conv)
            this.chatStore.addPrivateConv(conv)

            // Start message stream for new conversation if not already streaming
            const convId = Number(conv.id)
            if (!this.chatStore.activeStreams.messageStreams.has(convId)) {
              await this.startMessageStreamForConversation(conv.id)
            }
          }
        } catch (error: any) {
          if (error?.name === 'AbortError') {
            console.log('Conversation stream was cancelled')
            return
          }
          console.error('Error in conversation stream:', error)
          this.handleStreamError('conversation', error)
        }
      })()
    } catch (error: any) {
      console.error('Failed to start conversation stream:', error)
      throw error
    }
  }

  async startMessageStreams(conversations: PrivateConv[]) {
    const streamPromises = conversations.map((conv) =>
      this.startMessageStreamForConversation(conv.id)
    )

    const results = await Promise.allSettled(streamPromises)

    // Log any failed streams
    results.forEach((result, index) => {
      if (result.status === 'rejected') {
        console.error(
          `Failed to start message stream for conversation ${conversations[index].id}:`,
          result.reason,
        )
      }
    })
  }

  async startMessageStreamForConversation(convId: bigint) {
    try {
      // Create AbortController for this stream
      const controller = new AbortController()

      const stream = this.chatService.streamMessages({ convId })
      this.chatStore.addMessageStream(convId, stream, controller)

      // Handle message stream in background
      ;(async () => {
        try {
          for await (const msg of stream) {
            if (controller.signal.aborted) {
              break
            }
            console.log(`New message in conversation ${convId}:`, msg)
            this.chatStore.pushMessage(msg, convId)
          }
        } catch (error: any) {
          if (error.name === 'AbortError' || controller.signal.aborted) {
            console.log(`Message stream for conversation ${convId} was cancelled`)
            return
          }
          console.error(`Error in message stream for conversation ${convId}:`, error)
          this.handleStreamError('message', error, convId)
        } finally {
          // Cleanup stream reference
          this.chatStore.removeMessageStream(convId)
        }
      })()
    } catch (error: any) {
      console.error(`Failed to start message stream for conversation ${convId}:`, error)
      throw error
    }
  }

  handleStreamError(streamType: string, error: any, convId?: bigint) {
    console.error(`${streamType} stream error:`, error)

    // Implement reconnection logic with exponential backoff
    const retryDelay = 5000 // 5 seconds
    setTimeout(() => {
      if (streamType === 'conversation') {
        console.log('Attempting to reconnect conversation stream...')
        this.startConversationStream().catch(console.error)
      } else if (streamType === 'message' && convId) {
        console.log(`Attempting to reconnect message stream for conversation ${convId}...`)
        this.startMessageStreamForConversation(convId).catch(console.error)
      }
    }, retryDelay)
  }

  cleanup() {
    this.chatStore.cleanupAllStreams()
  }
}

// Composable for easy usage
export function useChatStreams() {
  const chatStore = useChatStore()
  const authStore = useAuthStore()
  const chatService = useChatService()

  const streamManager = new ChatStreamManager(chatService, chatStore, authStore)

  const initializeChat = async () => {
    try {
      await streamManager.initializeChatStreams()
    } catch (error: any) {
      console.error('Failed to initialize chat:', error)
      // Handle error (show notification, etc.)
    }
  }

  const cleanup = () => {
    streamManager.cleanup()
  }

  // Cleanup on component unmount
  onUnmounted(() => {
    cleanup()
  })

  return {
    initializeChat,
    cleanup,
    chatStore,
    streamManager,
  }
}
