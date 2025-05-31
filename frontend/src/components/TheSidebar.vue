<template>
  <div
    class="w-16 bg-white dark:bg-gray-800 border-r border-gray-500 flex flex-col items-center py-4"
  >
    <div class="space-y-4 flex flex-col">
      <div
        v-for="(elm, n) in elms.top"
        :key="n"
        class="cursor-pointer p-4 rounded-full flex items-center justify-center"
        :class="
          appStore.activePanel === elm.name
            ? 'bg-lime-500/25 text-lime-200 '
            : 'bg-gray-200 dark:bg-gray-700 hover:bg-lime-500/25 hover:text-lime-200'
        "
        @click="elm.action"
      >
          <i :class="elm.icon" style="font-size: 1.125rem; position: relative;">
            <span v-if="Number(elm.counter) > 0" class="absolute -top-4 -right-4 text-xs font-bold flex items-center justify-center w-4 h-4 rounded-full bg-lime-500 text-lime-900">
              {{ elm.counter }}
            </span>
          </i>
      </div>
    </div>
    <div class="mt-auto">
      <div
        v-for="(elm, n) in elms.bottom"
        :key="n"
        class="cursor-pointer p-4 bg-gray-200 dark:bg-gray-700 rounded-full flex items-center justify-center"
        :class="
          appStore.activePanel === elm.name
            ? 'bg-lime-500/25 text-lime-200'
            : 'bg-gray-200 dark:bg-gray-700 hover:bg-lime-500/25 hover:text-lime-200'
        "
        @click="elm.action"
      >
        <i :class="elm.icon" style="font-size: 1.125rem" />
      </div>
    </div>
  </div>
  <SettingsModal />
</template>

<script setup lang="ts">
import { useAppStore } from '@/stores/app'
import SettingsModal from './SettingsModal.vue'
import { computed } from 'vue'
import { useNotifStore } from '@/stores/notification'

const appStore = useAppStore()
const notifStore = useNotifStore()

const elms = {
  top: [
    {
      icon: 'pi pi-bell',
      action: () => appStore.setActivePanel('notification'),
      name: 'notification',
      counter: computed(() => notifStore.unreadCount)
    },
    {
      icon: 'pi pi-comments',
      action: () => appStore.setActivePanel('chat'),
      name: 'chat',
      counter: 0
    },
    {
      icon: 'pi pi-users',
      action: () => appStore.setActivePanel('group'),
      name: 'group',
      counter: 0
    },
  ],
  bottom: [
    {
      icon: 'pi pi-cog',
      action: () => (appStore.settingsModalVisible = true),
      name: 'settings',
    },
  ],
}
</script>
