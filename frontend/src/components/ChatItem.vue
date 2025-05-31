<script setup lang="ts">
import Avatar from 'primevue/avatar'
import { useRouter } from 'vue-router'
import type { PrivateConv } from '@/grpc/chat/chat_pb'
import { timeAgo } from '@/libs/utils'
import { useChatStore } from '@/stores/chat'
import { useAuthStore } from '@/stores/auth'
import { computed } from 'vue'

const props = defineProps<{
  conversation: PrivateConv
}>()

const chatStore = useChatStore()
const { getUserId } = useAuthStore()
const router = useRouter()

// const unreadCount = computed(
//   () => chatStore.privateConv[props.conversation.id.toString() ?? ''].unreadCount,
// )

const lastMessage = computed(() => chatStore.privateConv[props.conversation.id.toString()].lastMessage)

const otherUser = () => {
  // console.log("user id!", getUserId());
  // console.log("conv user id:", props.conversation?.user1?.id);

  return Number(props.conversation?.user1?.id) === getUserId()
    ? props.conversation.user2
    : props.conversation.user1;
}

function handleClick(conv: PrivateConv) {
  chatStore.setCurrentConv(conv)
  //getPrivateConversation
  router.push({ name: 'chat', params: { id: Number(conv.id) } })
}
</script>

<template>
  <div
    class="flex items-center gap-3 p-3 rounded-lg cursor-pointer hover:bg-gray-900/25 dark:hover:bg-gray-700/25"
    :class="{
      'bg-gray-100 dark:bg-gray-700':
        chatStore.currentChat && chatStore.currentChat.id === otherUser()?.id,
    }"
    @click="handleClick(conversation)"
  >
    <Avatar image="/images/default_avatar.jpg" class="bg-primary" shape="circle" />
    <div class="flex-1 min-w-0">
      <div class="flex justify-between items-center">
        <h4 class="font-medium truncate">{{ otherUser()?.username }}</h4>
        <span v-if="conversation.lastUpdate?.seconds" class="text-xs text-gray-500">{{
          timeAgo(conversation.lastUpdate)
        }}</span>
      </div>
      <p class="text-sm text-gray-500 truncate">
        {{ lastMessage }}
      </p>
    </div>
    <!-- <div v-if="unreadCount > 0" class="flex-shrink-0">
      <Badge :value="unreadCount" severity="primary" />
    </div> -->
  </div>
</template>
