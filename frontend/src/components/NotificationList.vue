<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between">
      <h2 class="text-lg font-semibold text-gray-800 dark:text-white">Mentions</h2>
      <span class="text-lime-500/50 hover:text-lime-500 cursor-pointer">Mark all as read</span>
    </div>

    <ul class="space-y-2">
      <RouterLink
        v-for="notif in notifStore.notifications"
        :key="notif.id"
        class="block"
        :to="notif.link"
      >
        <li
          class="flex items-start p-4 rounded-xl shadow space-x-4 hover:bg-gray-50 dark:hover:bg-gray-700 transition"
          :class="notif.unread ? 'bg-lime-500/50 dark:bg-lime-500/25' : 'bg-white dark:bg-gray-700'"
        >
          <div class="flex-1">
            <span class="font-medium text-gray-900 dark:text-white">@{{ notif.title }}</span>
            <p class="text-sm text-gray-700 dark:text-gray-300">
              {{ notif.content }}
            </p>
            <div v-if="notif.createdAt" class="text-xs text-gray-400 mt-1">
              {{ timeAgo(notif.createdAt) }}
            </div>
          </div>
        </li>
      </RouterLink>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { timeAgo } from '@/libs/utils'
import { useNotifStore } from '@/stores/notification'
import { RouterLink } from 'vue-router'

const notifStore = useNotifStore()
</script>
