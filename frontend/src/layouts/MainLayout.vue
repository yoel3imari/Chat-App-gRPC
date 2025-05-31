<!-- src/components/ChatLayout.vue -->
<template>
  <div class="flex h-screen bg-blue-200">
    <Sidebar />
    <SidebarWrapper />
    <main class="flex-1 flex flex-col bg-white dark:bg-gray-800">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import Sidebar from '@/components/TheSidebar.vue'
import SidebarWrapper from '@/components/SidebarWrapper.vue'
import { onMounted, onUnmounted } from 'vue'
import { useNotificationService } from '@/composables/useNotifService'
import { useAuthStore } from '@/stores/auth'
import { useToast } from 'primevue/usetoast'
import { useNotifStore } from '@/stores/notification'
import { useChatStreams } from '@/composables/useChatStreams'

const notifService = useNotificationService()

const authStore = useAuthStore()
const notifStore = useNotifStore()
const toast = useToast()

const { initializeChat, cleanup } = useChatStreams()

onMounted(async () => {
  try {
    if (authStore.user) {
      notifStore.notifications = []
      notifStore.unreadCount = 0

      await initializeChat()

      /*****    get notifications   ****** */

      const res = await notifService.getAllNotifications({ userId: authStore.user.id.toString() })
      res.notifications.forEach((n) => {
        notifStore.pushNotif(n)
        if (n.unread) {
          notifStore.unreadCount++
        }
      })

      const stream = notifService.streamNotifications({
        userId: authStore.user.id.toString(),
      })

      for await (const notification of stream) {
        notifStore.pushNotif(notification)
        toast.add({
          severity: 'info',
          detail: notification.content,
          summary: notification.title,
        })
        if (notification.unread) {
          notifStore.unreadCount++
        }
      }
    } else {
      throw Error('User not defined')
    }
  } catch (error: any) {
    console.log(error)
    toast.add({
      severity: 'error',
      detail: error,
    })
  }
})

onUnmounted(() => cleanup())
</script>
